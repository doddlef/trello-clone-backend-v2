package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipUpdateQuery
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.repo.MemberRepo
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.kevin.trello_v2.tasks.service.MemberService
import org.kevin.trello_v2.tasks.service.vo.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MemberServiceImpl(
    private val memberRepo: MemberRepo,
    private val taskPathHelper: TaskPathHelper,
    private val accountRepo: AccountRepo,
): MemberService {
    private val logger = LoggerFactory.getLogger(MemberServiceImpl::class.java)

    override fun inviteMember(vo: InviteMemberVO): ApiResponse {
        val (guestUid, boardId, role, user) = vo

        // Validate the guest user exists
        val guest = (accountRepo.findByUid(guestUid)?.takeUnless { it.archived }
            ?: throw BadArgumentException("Guest user with UID $guestUid does not exist"))

        // Validate board and authority
        taskPathHelper.pathOfBoard(user.uid, boardId).boardView?.let {
            if (it.role != MembershipRole.ADMIN)
                throw BadArgumentException("Only admin can invite members to the board")
            if (it.closed)
                throw BadArgumentException("This board is read-only")
        } ?: throw BadArgumentException("Board not exist")

        // Check if the guest is already a member of the board
        val membership = memberRepo.findByKey(guest.uid, boardId)
        // if membership not exist, then insert it
        // if membership exist but is not active, then update it
        // else, throw an exception
        if (membership == null) {
            MembershipInsertQuery(
                boardId = boardId,
                userUid = guest.uid,
                role = role,
            ).let { query ->
                memberRepo.insert(query).takeIf { it == 1 }
                    ?: throw TrelloException("Failed to insert into database")
            }
        } else if (!membership.active) {
            MembershipUpdateQuery(
                boardId = boardId,
                userUid = guest.uid,
                role = role,
                active = true,
            ).let { query ->
                memberRepo.update(query).takeIf { it == 1 }
                    ?: throw TrelloException("Failed to update membership in database")
            }
        } else {
            throw BadArgumentException("User with UID ${guest.uid} is already a member of the board")
        }

        return ApiResponse.success()
            .message("invite member successfully")
            .build()
    }

    override fun updateMember(vo: UpdateMemberVO): ApiResponse {
        val (targetUid, boardId, role, user) = vo

        // validate the authority
        taskPathHelper.pathOfBoard(user.uid, boardId).boardView?.let {
            if (it.role != MembershipRole.ADMIN)
                throw BadArgumentException("Only admin can update membership role")
            if (it.closed)
                throw BadArgumentException("This board is read-only")
        } ?: throw BadArgumentException("Board not exist")

        // validate the target membership exists
        val membership = (memberRepo.findByKey(targetUid, boardId)?.takeIf { it.active }
            ?: throw BadArgumentException("Target membership does not exist"))

        // validate the target membership is not the same as the new role
        if (membership.role == role )
            throw BadArgumentException("No change happened")

        MembershipUpdateQuery(boardId, targetUid, role).let { query ->
            memberRepo.update(query).takeIf { it == 1 }
                ?: throw TrelloException("Failed to update membership in database")
        }

        return ApiResponse.success()
            .message("update member successfully")
            .build()
    }

    override fun removeMember(vo: RemoveMemberVO): ApiResponse {
        val (targetUid, boardId, user) = vo

        // validate board and membership exists
        val boardView = taskPathHelper.pathOfBoard(targetUid, boardId).boardView?.also {
            if (it.closed) throw BadArgumentException("This board is read-only")
        } ?: throw BadArgumentException("Board or membership not exist")

        if (targetUid != user.uid) {
            // only admin can remove other members
            memberRepo.findByKey(user.uid, boardId)?.takeIf { it.role == MembershipRole.ADMIN }
                ?: throw BadArgumentException("Only admin can remove members from the board")
        }

        // ensure that there is at least one admin left in the board
        if (boardView.role == MembershipRole.ADMIN)
            assertAdminExists(boardId, targetUid)

        MembershipUpdateQuery(
            boardId,
            targetUid,
            active = false,
            ).let { query ->
            memberRepo.update(query).takeIf { it == 1 }
                    ?: throw TrelloException("Failed to update membership in database")
        }

        return ApiResponse.success()
            .message("remove member successfully")
            .build()
    }

    private fun assertAdminExists(boardId: String, targetUid: String) {
        MembershipSearchQuery(
            boardId = boardId,
        ).let { query ->
            memberRepo.search(query)
                .filter { it.active && it.role == MembershipRole.ADMIN && it.userUid != targetUid }
                .takeIf { it.isNotEmpty() }
                ?: throw BadArgumentException("No other admin exists in the board")
        }
    }
}
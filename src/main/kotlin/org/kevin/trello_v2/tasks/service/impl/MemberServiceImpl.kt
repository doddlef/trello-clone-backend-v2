package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipUpdateQuery
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.repo.MemberRepo
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.kevin.trello_v2.tasks.service.MemberService
import org.kevin.trello_v2.tasks.service.vo.InviteMemberVO
import org.springframework.stereotype.Service

@Service
class MemberServiceImpl(
    private val memberRepo: MemberRepo,
    private val taskPathHelper: TaskPathHelper,
    private val accountRepo: AccountRepo,
): MemberService {
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
            ).let {
                memberRepo.insert(it).takeIf { it == 1 }
                    ?: throw TrelloException("Failed to insert into database")
            }
        } else if (!membership.active) {
            MembershipUpdateQuery(
                boardId = boardId,
                userUid = guest.uid,
                role = role,
                active = true,
            ).let {
                memberRepo.update(it).takeIf { it == 1 }
                    ?: throw TrelloException("Failed to update membership in database")
            }
        } else {
            throw BadArgumentException("User with UID ${guest.uid} is already a member of the board")
        }

        return ApiResponse.success()
            .message("invite member successfully")
            .build()
    }
}
package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.BoardService
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.springframework.stereotype.Service
import org.kevin.trello_v2.tasks.constants.*
import org.kevin.trello_v2.tasks.mapper.BoardMapper
import org.kevin.trello_v2.tasks.mapper.BoardMemberMapper
import org.kevin.trello_v2.tasks.mapper.BoardViewMapper
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardUpdateQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.ViewSearchQuery
import org.kevin.trello_v2.tasks.model.BoardDto
import org.kevin.trello_v2.tasks.model.BoardView
import org.kevin.trello_v2.tasks.model.MembershipDto
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.kevin.trello_v2.tasks.service.vo.ArchiveBoardVO
import org.kevin.trello_v2.tasks.service.vo.CloseBoardVO
import org.kevin.trello_v2.tasks.service.vo.UpdateBoardVO
import org.springframework.transaction.annotation.Transactional

@Service
class BoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val boardMemberMapper: BoardMemberMapper,
    private val boardViewMapper: BoardViewMapper,
    private val taskPathHelper: TaskPathHelper,
): BoardService {
    private fun validateTitle(title: String) {
        if (title.isBlank()) throw BadArgumentException("Title cannot be blank")
        if (title.length > MAX_BOARD_TITLE_LENGTH) throw BadArgumentException("Title cannot exceed $MAX_BOARD_TITLE_LENGTH characters")
    }

    private fun validateDescription(description: String) {
        if (description.isBlank()) throw BadArgumentException("Description cannot be blank")
        if (description.length > MAX_BOARD_DESCRIPTION_LENGTH) throw BadArgumentException("Description exceeds maximum length of $MAX_BOARD_DESCRIPTION_LENGTH characters")
    }

    override fun listOfBoard(user: Account): ApiResponse {
        ViewSearchQuery(userUid = user.uid)
            .let {
                val views = boardViewMapper.search(it)
                return ApiResponse.success()
                    .add("boards" to views)
                    .build()
            }
    }

    @Transactional
    override fun createBoard(vo: CreateBoardVO): ApiResponse {
        // ("validate vo")
        validateTitle(vo.title)
        vo.description?.let { validateDescription(it) }

        val (title, description, user) = vo
        // ("create board in database")
        val board = BoardInsertQuery(
            title = title,
            description = description,
            createdBy = user.uid
        ).let {
            val count = boardMapper.insert(it)
            if (count != 1) throw TrelloException("Failed to create board in database")
            BoardDto(id = it.id, title = it.title, description = it.description, closed = false)
        }

        // ("create board member in database")
        val membership = MembershipInsertQuery(
            boardId = board.id,
            userUid = user.uid,
            role = MembershipRole.ADMIN,
            starred = false,
        ).let {
            val count = boardMemberMapper.insert(it)
            if (count != 1) throw TrelloException("Failed to create board member in database")
            MembershipDto(boardId = it.boardId, userUid = it.userUid, role = it.role, starred = it.starred)
        }

        return ApiResponse.success()
            .message("Board created successfully")
            .add("board" to BoardView(board, membership))
            .build()
    }

    @Transactional
    override fun updateBoard(vo: UpdateBoardVO): ApiResponse {
        // ("validate vo")
        if (vo.isAllNull()) {
            throw BadArgumentException("No change is happened")
        }
        vo.title?.let { validateTitle(vo.title) }
        vo.description?.let { validateDescription(it) }

        // validate user permission
        validateBoardModification(vo.user.uid, vo.boardId)

        // update board in database
        BoardUpdateQuery(
            id = vo.user.uid,
            title = vo.title,
            description = vo.description,
        ).let {
            boardMapper.updateById(it).takeUnless { it == 1 }
                ?: throw TrelloException("Failed to update board")
        }

        return ApiResponse.success()
            .message("Board updated successfully")
            .build()
    }

    @Transactional
    override fun closeBoard(vo: CloseBoardVO): ApiResponse {
        val (boardId, user) = vo
        validateBoardModification(user.uid, boardId)

        BoardUpdateQuery(
            id = vo.boardId,
            closed = true,
        ).let {
            boardMapper.updateById(it).takeIf { it == 1 }
                ?: throw TrelloException("Failed to update board")
        }

        return ApiResponse.success()
            .message("Board has been closed successfully")
            .build()
    }

    @Transactional
    override fun archiveBoard(vo: ArchiveBoardVO): ApiResponse {
        taskPathHelper.pathOfBoard(vo.user.uid, vo.boardId).board?.let {
            if (it.role != MembershipRole.ADMIN) {
                throw BadArgumentException("User does not have permission to archive this board")
            }
        } ?: throw BadArgumentException("board not exist")

        // update board in database
        BoardUpdateQuery(
            id = vo.boardId,
            archived = true,
        ).let {
            boardMapper.updateById(it).takeIf { it == 1 }
                ?: throw TrelloException("Failed to update board")
        }

        return ApiResponse.success()
            .message("Board archived successfully")
            .build()
    }

    private fun validateBoardModification(userUid: String, boardId: String) {
        taskPathHelper.pathOfBoard(userUid, boardId).board?.let {
            if (it.closed) {
                throw BadArgumentException("This board is read-only")
            }
            if (it.role != MembershipRole.ADMIN) {
                throw BadArgumentException("User does not have permission to modify this board")
            }
        } ?: throw BadArgumentException("board not exist")
    }
}
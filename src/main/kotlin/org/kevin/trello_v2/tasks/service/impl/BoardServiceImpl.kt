package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.BoardService
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.springframework.stereotype.Service
import org.kevin.trello_v2.tasks.constants.*
import org.kevin.trello_v2.tasks.mapper.BoardMapper
import org.kevin.trello_v2.tasks.mapper.BoardMemberMapper
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.model.MembershipRole

@Service
class BoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val boardMemberMapper: BoardMemberMapper,
): BoardService {
    fun validateCreateBoardVO(vo: CreateBoardVO) {
        // ("validate title")
        if (vo.title.isBlank()) throw BadArgumentException("Title cannot be blank")
        if (vo.title.length > MAX_BOARD_TITLE_LENGTH) throw BadArgumentException("Title exceeds maximum length of $MAX_BOARD_TITLE_LENGTH characters")

        // ("validate description")
        if (vo.description != null) {
            if (vo.description.isBlank()) throw BadArgumentException("Description cannot be blank")
            if (vo.description.length > MAX_BOARD_DESCRIPTION_LENGTH) throw BadArgumentException("Description exceeds maximum length of 1024 characters")
        }
    }

    override fun createBoard(vo: CreateBoardVO): ApiResponse {
        // ("validate vo")
        validateCreateBoardVO(vo)

        val (title, description, visibility, user) = vo
        // ("create board in database")
        val boardId = BoardInsertQuery(
            title = title,
            description = description,
            visibility = visibility,
            createdBy = user.uid
        ).let {
            val count = boardMapper.insert(it)
            if (count != 1) throw TrelloException("Failed to create board in database")
            it.id
        }

        // ("create board member in database")
        val membership = MembershipInsertQuery(
            boardId = boardId,
            userUid = user.uid,
            role = MembershipRole.ADMIN,
            starred = false,
        ).let {
            val count = boardMemberMapper.insert(it)
            if (count != 1) throw TrelloException("Failed to create board member in database")
        }

        return ApiResponse.success()
            .message("Board created successfully")
            .add("boardId" to boardId)
            .build()
    }
}
package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.ArchiveBoardVO
import org.kevin.trello_v2.tasks.service.vo.CloseBoardVO
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.kevin.trello_v2.tasks.service.vo.UpdateBoardVO

interface BoardService {
    fun boardContent(boardId: String, reader: Account): ApiResponse

    fun listOfBoard(user: Account): ApiResponse

    /**
     * Creates a board with the given [CreateBoardVO].
     * If the title is blank or exceeds the maximum length, an exception will be thrown.
     * If the description is provided, it must not be blank and must not exceed the maximum length.
     */
    fun createBoard(vo: CreateBoardVO): ApiResponse

    /**
     * Updates a board with the given [UpdateBoardVO].
     * If all fields in [UpdateBoardVO] are null, the update will not be performed.
     */
    fun updateBoard(vo: UpdateBoardVO): ApiResponse

    /**
     * Closes a board with the given [CloseBoardVO].
     * If the board is already closed, it will not be closed again.
     */
    fun closeBoard(vo: CloseBoardVO): ApiResponse

    /**
     * Archives a board with the given [ArchiveBoardVO].
     * If the board is already archived, it will not be archived again.
     */
    fun archiveBoard(vo: ArchiveBoardVO): ApiResponse
}
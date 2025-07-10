package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.kevin.trello_v2.tasks.service.vo.UpdateBoardVO

interface BoardService {
    fun createBoard(vo: CreateBoardVO): ApiResponse

    /**
     * Updates a board with the given [UpdateBoardVO].
     * If all fields in [UpdateBoardVO] are null, the update will not be performed.
     */
    fun updateBoard(vo: UpdateBoardVO): ApiResponse
}
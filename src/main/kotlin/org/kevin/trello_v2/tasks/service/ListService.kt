package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.CreateListVO

interface ListService {
    /**
     * Create a new list in the board.
     * It will be put at the end of existed lists of board.
     * The user must be a Member or Admin of the board, and the board is not closed.
     */
    fun createList(vo: CreateListVO): ApiResponse
}
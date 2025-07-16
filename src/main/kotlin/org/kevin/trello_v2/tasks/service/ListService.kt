package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.CreateListVO
import org.kevin.trello_v2.tasks.service.vo.EditListVO
import org.kevin.trello_v2.tasks.service.vo.MoveListVO

interface ListService {
    /**
     * Create a new list in the board.
     * It will be put at the end of existed lists of board.
     * The user must be a Member or Admin of the board, and the board is not closed.
     */
    fun createList(vo: CreateListVO): ApiResponse

    /**
     * Update the list.
     * The user must be a Member or Admin of the board, and the board is not closed.
     */
    fun editList(vo: EditListVO): ApiResponse

    /**
     * Move a list to another position in the board.
     * The user must be a Member or Admin of the board, and the board is not closed.
     *
     * @param vo The MoveListVO containing the list ID and the position to move it to.
     */
    fun moveList(vo: MoveListVO): ApiResponse
}
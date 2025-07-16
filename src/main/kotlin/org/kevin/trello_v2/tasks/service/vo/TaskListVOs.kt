package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account

data class CreateListVO(
    val title: String,
    val boardId: String,
    val user: Account,
)

data class EditListVO(
    val listId: Long,
    val title: String? = null,
    val color: String? = null,
    val clearColor: Boolean? = null,
    val user: Account,
) {
    fun allIsNull(): Boolean =
        title == null && color == null && clearColor == null
}

/**
 * Move a list to another position in the board.
 * The user must be a Member or Admin of the board, and the board is not closed.
 *
 * @param listId The ID of the list to move.
 * @param afterId Put it after list whit ID.
 */
data class MoveListVO(
    val listId: Long,
    val afterId: Long?,
    val user: Account,
)
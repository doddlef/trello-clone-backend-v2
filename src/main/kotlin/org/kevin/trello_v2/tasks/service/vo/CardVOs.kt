package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account
import java.time.LocalDate

data class CreateCardVO(
    val title: String,
    val listId: Long,
    val user: Account,
)

data class CardEditVO(
    val title: String? = null,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val cardId: Long,
    val user: Account,
) {
    fun isEmpty(): Boolean {
        return title == null && description == null && dueDate == null
    }
}

/**
 * Represents a card move operation.
 *
 * @param cardId The ID of the card to be moved.
 * @param listId The ID of the list to which the card should be moved.
 * This should be set if the card is being moved to a different list.
 * @param afterId The ID of the card after which this card should be placed.
 * Null if the card is being moved to the start of the list.
 */
data class CardMoveVO(
    val cardId: Long,
    val listId: Long? = null,
    val afterId: Long? = null,
    val user: Account
)
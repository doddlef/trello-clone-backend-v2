package org.kevin.trello_v2.tasks.mapper.queries

import java.time.LocalDate

data class CardInsertQuery(
    val title: String,
    val position: Double,
    val listId: Long,
) {
    var id: Long? = null
}

data class CardUpdateQuery(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val finished: Boolean? = null,
    val position: Double? = null,
    val dueDate: LocalDate? = null,
    val listId: Long? = null,
    val archived: Boolean? = null,
)

data class CardSearchQuery(
    val listId: Long? = null,
    val archived: Boolean? = false,
)
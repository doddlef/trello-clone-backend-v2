package org.kevin.trello_v2.tasks.model

import java.time.LocalDateTime
import kotlin.Long

data class Card(
    val id: Long,
    val title: String,
    val description: String?,
    val finished: Boolean,
    val position: Double,
    val dueDate: LocalDateTime?,
    val listId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val archived: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun asDto(): CardDto {
        return CardDto(
            id = id,
            title = title,
            description = description,
            finished = finished,
            position = position,
            dueDate = dueDate,
            listId = listId,
        )
    }
}

data class CardDto(
    val id: Long,
    val title: String,
    val description: String?,
    val finished: Boolean,
    val position: Double,
    val dueDate: LocalDateTime?,
    val listId: Long,
)
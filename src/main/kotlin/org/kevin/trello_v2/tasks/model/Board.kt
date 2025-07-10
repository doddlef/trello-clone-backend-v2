package org.kevin.trello_v2.tasks.model

import java.time.LocalDateTime

data class Board(
    val id: String,
    val title: String,
    val description: String?,
    val closed: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String,
    val archived: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun asDto(): BoardDto {
        return BoardDto(
            id = id,
            title = title,
            description = description,
            closed = closed,
        )
    }
}

data class BoardDto(
    val id: String,
    val title: String,
    val description: String?,
    val closed: Boolean,
)
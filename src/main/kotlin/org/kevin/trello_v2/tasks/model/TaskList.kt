package org.kevin.trello_v2.tasks.model

import java.time.LocalDateTime

data class TaskList(
    val id: Long,
    val title: String,
    val color: String?,
    val position: Double,
    val boardId: String,
    val createdAt: LocalDateTime,
    val archived: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskList

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun asDto(): TaskListDto {
        return TaskListDto(
            id = id,
            title = title,
            color = color,
            position = position,
            boardId = boardId,
        )
    }
}

data class TaskListDto(
    val id: Long,
    val title: String,
    val color: String?,
    val position: Double,
    val boardId: String,
)

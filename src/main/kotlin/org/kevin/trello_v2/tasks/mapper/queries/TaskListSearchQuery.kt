package org.kevin.trello_v2.tasks.mapper.queries

data class TaskListSearchQuery(
    val boardId: String? = null,
    val archived: Boolean = false,
)

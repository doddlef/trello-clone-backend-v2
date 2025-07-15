package org.kevin.trello_v2.tasks.mapper.queries

data class TaskListUpdateQuery(
    val id: Long,
    val title: String? = null,
    val color: String? = null,
    val clearColor: Boolean = false,
    val position: Double? = null,
    val archived: Boolean? = null,
)

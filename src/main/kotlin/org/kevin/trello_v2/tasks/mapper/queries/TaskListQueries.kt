package org.kevin.trello_v2.tasks.mapper.queries

data class TaskListInsertQuery(
    val title: String,
    val color: String? = null,
    val position: Double,
    val boardId: String,
) {
    var id: Long? = null
}

data class TaskListUpdateQuery(
    val id: Long,
    val title: String? = null,
    val color: String? = null,
    val clearColor: Boolean = false,
    val position: Double? = null,
    val archived: Boolean? = null,
)

data class TaskListSearchQuery(
    val boardId: String? = null,
    val archived: Boolean = false,
)
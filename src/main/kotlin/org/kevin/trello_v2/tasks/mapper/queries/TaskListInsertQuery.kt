package org.kevin.trello_v2.tasks.mapper.queries

data class TaskListInsertQuery(
    val title: String,
    val color: String? = null,
    val position: Double,
    val boardId: String,
) {
    var id: Long? = null
}

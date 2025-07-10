package org.kevin.trello_v2.tasks.mapper.queries

data class BoardUpdateQuery(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val closed: Boolean? = null,
    val archived: Boolean? = null,
)

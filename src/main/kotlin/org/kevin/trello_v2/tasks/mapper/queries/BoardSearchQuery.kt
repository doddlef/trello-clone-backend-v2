package org.kevin.trello_v2.tasks.mapper.queries

data class BoardSearchQuery(
    val ids: List<String>? = null,
    val isArchived: Boolean? = false,
)

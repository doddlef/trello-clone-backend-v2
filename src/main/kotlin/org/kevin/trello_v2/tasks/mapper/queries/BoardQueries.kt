package org.kevin.trello_v2.tasks.mapper.queries

import com.github.f4b6a3.ulid.UlidCreator

data class BoardInsertQuery(
    val title: String,
    val description: String?,
    val createdBy: String,
) {
    val id = UlidCreator.getMonotonicUlid().toString()
}

data class BoardSearchQuery(
    val ids: List<String>? = null,
    val isArchived: Boolean? = false,
)

data class BoardUpdateQuery(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val closed: Boolean? = null,
    val archived: Boolean? = null,
)
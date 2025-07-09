package org.kevin.trello_v2.tasks.mapper.queries

import com.github.f4b6a3.ulid.UlidCreator
import org.kevin.trello_v2.tasks.model.BoardVisibility

data class BoardInsertQuery(
    val title: String,
    val description: String?,
    val visibility: BoardVisibility,
    val createdBy: String,
) {
    val id = UlidCreator.getMonotonicUlid().toString()
}

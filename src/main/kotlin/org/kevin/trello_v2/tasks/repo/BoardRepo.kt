package org.kevin.trello_v2.tasks.repo

import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardUpdateQuery
import org.kevin.trello_v2.tasks.model.Board

interface BoardRepo {
    fun search(query: BoardSearchQuery): List<Board>

    fun findById(id: String): Board?

    fun insert(query: BoardInsertQuery): Int

    fun updateById(query: BoardUpdateQuery): Int
}
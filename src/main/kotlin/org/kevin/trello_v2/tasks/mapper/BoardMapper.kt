package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardUpdateQuery

@Mapper
interface BoardMapper {
    fun insert(query: BoardInsertQuery): Int
    fun updateById(query: BoardUpdateQuery): Int
}
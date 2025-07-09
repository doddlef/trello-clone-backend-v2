package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery

@Mapper
interface BoardMapper {
    fun insert(query: BoardInsertQuery): Int
}
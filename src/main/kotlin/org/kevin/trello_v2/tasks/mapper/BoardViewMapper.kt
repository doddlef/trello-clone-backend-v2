package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.kevin.trello_v2.tasks.mapper.queries.ViewSearchQuery
import org.kevin.trello_v2.tasks.model.BoardView

@Mapper
interface BoardViewMapper {
    fun findByUserAndBoard(
        @Param("userUid") userUid: String,
        @Param("boardId") boardId: String
    ): BoardView?

    fun search(query: ViewSearchQuery): List<BoardView>
}
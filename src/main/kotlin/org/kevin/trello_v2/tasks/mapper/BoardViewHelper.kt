package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.kevin.trello_v2.tasks.mapper.queries.ViewSearchQuery
import org.kevin.trello_v2.tasks.model.BoardView

/**
 * provide methods to access board view data
 * board view is read-only data represent a board and membership entry
 * this will only view entry with not archived board and active membership
 */
@Mapper
interface BoardViewHelper {
    fun findByUserAndBoard(
        @Param("userUid") userUid: String,
        @Param("boardId") boardId: String
    ): BoardView?

    fun search(query: ViewSearchQuery): List<BoardView>
}
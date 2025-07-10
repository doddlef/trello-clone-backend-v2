package org.kevin.trello_v2.tasks.repo.impl

import org.kevin.trello_v2.tasks.mapper.BoardViewMapper
import org.kevin.trello_v2.tasks.repo.PathResult
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.springframework.stereotype.Repository

@Repository
class TaskPathHelperImpl(
    private val boardViewMapper: BoardViewMapper,
): TaskPathHelper {

    override fun pathOfBoard(
        userUid: String,
        boardId: String
    ): PathResult {
        val board = boardViewMapper.findByUserAndBoard(userUid, boardId)
        return PathResult(board = board)
    }
}
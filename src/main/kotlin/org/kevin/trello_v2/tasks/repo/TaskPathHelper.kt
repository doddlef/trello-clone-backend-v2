package org.kevin.trello_v2.tasks.repo

import org.kevin.trello_v2.tasks.model.BoardView

interface TaskPathHelper {
    fun pathOfBoard(userUid: String, boardId: String): PathResult
}

data class PathResult (
    val board: BoardView?,
)
package org.kevin.trello_v2.tasks.repo

import org.kevin.trello_v2.tasks.model.Board
import org.kevin.trello_v2.tasks.model.BoardMembership
import org.kevin.trello_v2.tasks.model.BoardView

interface TaskPathHelper {
    fun pathOfBoard(userUid: String, boardId: String): PathResult
}

data class PathResult(
    val board: Board? = null,
    val member: BoardMembership? = null
) {
    val boardView: BoardView? by lazy {
        board?.let { b -> member?.let { m -> BoardView(b, m) } }
    }
}
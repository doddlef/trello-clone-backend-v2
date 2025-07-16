package org.kevin.trello_v2.tasks.repo

import org.kevin.trello_v2.tasks.model.Board
import org.kevin.trello_v2.tasks.model.BoardMembership
import org.kevin.trello_v2.tasks.model.BoardView
import org.kevin.trello_v2.tasks.model.Card
import org.kevin.trello_v2.tasks.model.TaskList

/**
 * TaskPathHelper is used to resolve the path of a item
 *
 * it only consider not archived items and active memberships
 */
interface TaskPathHelper {
    fun pathOfBoard(userUid: String, boardId: String): PathResult
    fun pathOfTaskList(userUid: String, listId: Long): PathResult
    fun pathOfCard(userUid: String, cardId: Long): PathResult
}

data class PathResult(
    val board: Board? = null,
    val member: BoardMembership? = null,
    val taskList: TaskList? = null,
    val card: Card? = null,
) {
    val boardView: BoardView? by lazy {
        board?.let { b -> member?.let { m -> BoardView(b, m) } }
    }
}
package org.kevin.trello_v2.tasks.repo.impl

import org.kevin.trello_v2.tasks.mapper.TaskListMapper
import org.kevin.trello_v2.tasks.repo.BoardRepo
import org.kevin.trello_v2.tasks.repo.MemberRepo
import org.kevin.trello_v2.tasks.repo.PathResult
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.springframework.stereotype.Repository

@Repository
class TaskPathHelperImpl(
    private val boardRepo: BoardRepo,
    private val membersRepo: MemberRepo,
    private val taskListMapper: TaskListMapper,
): TaskPathHelper {

    override fun pathOfBoard(
        userUid: String,
        boardId: String
    ): PathResult {
        val board = boardRepo.findById(boardId)?.takeUnless { it.archived }
        val member = board?.let { membersRepo.findByKey(userUid, boardId)?.takeIf { it.active } }
        return PathResult(board, member)
    }

    override fun pathOfTaskList(
        userUid: String,
        listId: Long
    ): PathResult {
        val taskList = taskListMapper.findById(listId)?.takeUnless { it.archived }
        val board = taskList?.let { boardRepo.findById(it.boardId)?.takeUnless { board -> board.archived }}
        val member = board?.let { membersRepo.findByKey(userUid, it.id)?.takeIf { m -> m.active }}
        return PathResult(board, member, taskList)
    }
}
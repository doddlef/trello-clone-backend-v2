package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.kevin.trello_v2.tasks.service.ListService
import org.kevin.trello_v2.tasks.service.vo.CreateListVO
import org.kevin.trello_v2.tasks.constants.*
import org.kevin.trello_v2.tasks.mapper.TaskListMapper
import org.kevin.trello_v2.tasks.mapper.queries.TaskListInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.TaskListSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.TaskListUpdateQuery
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.service.vo.EditListVO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListServiceImpl(
    private val pathHelper: TaskPathHelper,
    private val listMapper: TaskListMapper,
): ListService {
    private fun validateListTitle(title: String) {
        if (title.isBlank()) throw IllegalArgumentException("List title cannot be blank")
        if (title.length > MAX_LIST_TITLE_LENGTH)
            throw IllegalArgumentException("List title cannot exceed $MAX_LIST_TITLE_LENGTH characters")
    }

    private fun validateListColor(color: String) {
        if (!color.matches(Regex("^#([A-Fa-f0-9]{6})$")))
            throw IllegalArgumentException("Color must be in hex format, e.g. #f0f0f0")
    }

    @Transactional
    override fun createList(vo: CreateListVO): ApiResponse {
        val (title, boardId, user) = vo

        // validate title
        validateListTitle(title)

        // validate permission
        pathHelper.pathOfBoard(userUid = user.uid, boardId = boardId).boardView?.let {
            if (it.closed)
                throw TrelloException("Board is closed, no modifications allowed")
            if (it.role == MembershipRole.VIEWER)
                throw TrelloException("Viewers can only read board content")
        } ?: throw TrelloException("Board not exists")

        // decide the position of new list
        val position = TaskListSearchQuery(boardId = boardId)
            .let { listMapper.search(it) }
            .maxOfOrNull { it.position } ?: 0.0
            .plus(LIST_POSITION_INTERVAL)

        // insert into database
        val listId = TaskListInsertQuery(
            title = title,
            position = position,
            boardId = boardId,
        ).let {
            val count = listMapper.insert(it)
            val id = it.id
            if (count != 1 || id == null) throw TrelloException("Failed to create list")
            id
        }

        return ApiResponse.success()
            .message("create list success")
            .add("listId" to listId)
            .build()
    }

    @Transactional
    override fun editList(vo: EditListVO): ApiResponse {
        val (listId, title, color, clearColor, user) = vo

        // validate values
        if (vo.allIsNull()) throw TrelloException("No change happened")
        if (title != null) validateListTitle(title)
        if (color != null) validateListColor(color)
        if (color != null && clearColor != null)
            throw TrelloException("Cannot set both color and clearColor at the same time")

        // validate permission and existence
        pathHelper.pathOfTaskList(userUid = user.uid, listId = listId).boardView?.let {
            if (it.closed) throw TrelloException("Board is closed, no modifications allowed")
            if (it.role == MembershipRole.VIEWER) throw TrelloException("Viewers can only read board content")
        } ?: throw TrelloException("List not exists")

        // update the list
        TaskListUpdateQuery(
            id = listId,
            title = title,
            color = color,
            clearColor = clearColor ?: false,
        ).let { query ->
            val count = listMapper.update(query)
            if (count != 1) throw TrelloException("Failed to update list")
        }

        return ApiResponse.success()
            .message("edit list success")
            .build()
    }
}
package org.kevin.trello_v2.tasks.controller

import org.kevin.trello_v2.auth.context.AccountContext
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.controller.requests.CreateBoardRequest
import org.kevin.trello_v2.tasks.model.BoardVisibility
import org.kevin.trello_v2.tasks.service.BoardService
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class TaskController(
    private val boardService: BoardService,
) {
    @PostMapping("/board")
    fun createBoard(@RequestBody request: CreateBoardRequest): ApiResponse {
        val (title, description, visibility) = request
        val account = AccountContext.currentAccountOrThrow()

        CreateBoardVO(
            title = title,
            description = description,
            visibility = BoardVisibility.fromString(visibility),
            user = account,
        ).let {
            return boardService.createBoard(it)
        }
    }
}
package org.kevin.trello_v2.tasks.controller

import org.kevin.trello_v2.auth.context.AccountContext
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.controller.requests.CreateBoardRequest
import org.kevin.trello_v2.tasks.controller.requests.UpdateBoardRequest
import org.kevin.trello_v2.tasks.service.BoardService
import org.kevin.trello_v2.tasks.service.vo.ArchiveBoardVO
import org.kevin.trello_v2.tasks.service.vo.CloseBoardVO
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO
import org.kevin.trello_v2.tasks.service.vo.UpdateBoardVO
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        val (title, description) = request
        val account = AccountContext.currentAccountOrThrow()

        CreateBoardVO(
            title = title,
            description = description,
            user = account,
        ).let {
            return boardService.createBoard(it)
        }
    }

    @PutMapping("/board/{id}")
    fun updateBoard(
        @RequestBody request: UpdateBoardRequest,
        @PathVariable("id") id: String,
    ): ApiResponse {
        val (title, description) = request
        val account = AccountContext.currentAccountOrThrow()

        UpdateBoardVO(
            boardId = id,
            title = title,
            description = description,
            user = account,
        ).let {
            return boardService.updateBoard(it)
        }
    }

    @DeleteMapping("/board/{id}")
    fun closeBoard(@PathVariable("id") id: String): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()

        CloseBoardVO(
            boardId = id,
            user = account,
        ).let {
            return boardService.closeBoard(it)
        }
    }


    @DeleteMapping("/board/{id}/archive")
    fun archiveBoard(@PathVariable("id") id: String): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()

        ArchiveBoardVO(
            boardId = id,
            user = account,
        ).let {
            return boardService.archiveBoard(it)
        }
    }
}
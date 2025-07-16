package org.kevin.trello_v2.tasks.controller

import org.kevin.trello_v2.auth.context.AccountContext
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.service.BoardService
import org.kevin.trello_v2.tasks.service.ListService
import org.kevin.trello_v2.tasks.service.MemberService
import org.kevin.trello_v2.tasks.service.vo.*
import org.kevin.trello_v2.tasks.controller.requests.*
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
    private val memberService: MemberService,
    private val listService: ListService,
) {
    @GetMapping("/board-list")
    fun boardList(): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()
        return boardService.listOfBoard(account)
    }

    @GetMapping("/board/{id}")
    fun boardContent(
        @PathVariable("id") id: String,
    ): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()
        return boardService.boardContent(id, account)
    }

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

    @PostMapping("/board/{id}/invite")
    fun inviteMember(
        @PathVariable("id") id: String,
        @RequestBody request: InviteMemberRequest,
    ): ApiResponse {
        val (guestUid, roleStr) = request
        val role = MembershipRole.fromString(roleStr)
        val account = AccountContext.currentAccountOrThrow()

        InviteMemberVO(
            boardId = id,
            guestUid = guestUid,
            role = role,
            user = account,
        ).let {
            return memberService.inviteMember(it)
        }
    }

    @PutMapping("/board/{id}/member/{targetUid}")
    fun updateMember(
        @PathVariable("id") id: String,
        @PathVariable("targetUid") targetUid: String,
        @RequestBody request: UpdateMemberRequest,
    ): ApiResponse {
        val (roleStr) = request
        val role = MembershipRole.fromString(roleStr)
        val account = AccountContext.currentAccountOrThrow()

        UpdateMemberVO(
            targetUid = targetUid,
            boardId = id,
            role = role,
            user = account,
        ).let {
            return memberService.updateMember(it)
        }
    }

    @DeleteMapping("/board/{id}/member/{targetUid}")
    fun removeMember(
        @PathVariable("id") id: String,
        @PathVariable("targetUid") targetUid: String,
    ): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()

        RemoveMemberVO(
            targetUid = targetUid,
            boardId = id,
            user = account,
        ).let {
            return memberService.removeMember(it)
        }
    }

    @PostMapping("/board/{id}/list")
    fun createList(
        @PathVariable("id") id: String,
        @RequestBody request: CreateListRequest,
    ): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()
        val (title) = request

        CreateListVO(
            title = title,
            boardId = id,
            user = account,
        ).let {
            return listService.createList(it)
        }
    }

    @PutMapping("/list/{id}")
    fun editList(
        @PathVariable("id") id: Long,
        @RequestBody request: UpdateListRequest,
    ): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()
        val (title, color, clearColor) = request

        EditListVO(
            listId = id,
            title = title,
            color = color,
            clearColor = clearColor,
            user = account,
        ).let {
            return listService.editList(it)
        }
    }

    @PutMapping("/list/{id}/move")
    fun moveList(
        @PathVariable("id") id: Long,
        @RequestBody request: MoveListRequest,
    ): ApiResponse {
        val account = AccountContext.currentAccountOrThrow()
        val (afterId) = request

        MoveListVO(
            listId = id,
            afterId = afterId,
            user = account,
        ).let {
            return listService.moveList(it)
        }
    }
}
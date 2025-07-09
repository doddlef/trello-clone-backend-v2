package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.CreateBoardVO

interface BoardService {
    fun createBoard(vo: CreateBoardVO): ApiResponse
}
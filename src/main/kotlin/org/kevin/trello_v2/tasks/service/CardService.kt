package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.*

interface CardService {
    fun createCard(vo: CreateCardVO): ApiResponse
    fun editCard(vo: CardEditVO): ApiResponse
    fun moveCard(vo: CardMoveVO): ApiResponse
    fun completeTask(cardId: Long, user: Account): ApiResponse
    fun incompleteTask(cardId: Long, user: Account): ApiResponse
    fun archiveTask(cardId: Long, user: Account): ApiResponse
}
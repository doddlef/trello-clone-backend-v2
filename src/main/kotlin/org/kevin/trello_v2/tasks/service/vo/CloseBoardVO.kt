package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account

data class CloseBoardVO(
    val boardId: String,
    val user: Account,
)

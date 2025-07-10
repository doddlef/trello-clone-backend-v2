package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account

data class CreateBoardVO(
    val title: String,
    val description: String?,
    val user: Account,
)
package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.tasks.model.BoardVisibility

data class CreateBoardVO(
    val title: String,
    val description: String?,
    val visibility: BoardVisibility,
    val user: Account,
)
package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account

data class CreateBoardVO(
    val title: String,
    val description: String?,
    val user: Account,
)

data class UpdateBoardVO(
    val boardId: String,
    val title: String?,
    val description: String?,
    val user: Account
) {
    fun isAllNull(): Boolean =
        title == null && description == null
}

data class CloseBoardVO(
    val boardId: String,
    val user: Account,
)

data class ArchiveBoardVO(
    val boardId: String,
    val user: Account,
)
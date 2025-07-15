package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account

data class EditListVO(
    val listId: Long,
    val title: String? = null,
    val color: String? = null,
    val clearColor: Boolean? = null,
    val user: Account,
) {
    fun allIsNull(): Boolean =
        title == null && color == null && clearColor == null
}

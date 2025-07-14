package org.kevin.trello_v2.tasks.service.vo

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.tasks.model.MembershipRole

data class UpdateMemberVO(
    val targetUid: String,
    val boardId: String,
    val role: MembershipRole,
    val user: Account,
)

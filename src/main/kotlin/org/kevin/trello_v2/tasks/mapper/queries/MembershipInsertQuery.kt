package org.kevin.trello_v2.tasks.mapper.queries

import org.kevin.trello_v2.tasks.model.MembershipRole

data class MembershipInsertQuery (
    val boardId: String,
    val userUid: String,
    val role: MembershipRole,
    val starred: Boolean = false,
)
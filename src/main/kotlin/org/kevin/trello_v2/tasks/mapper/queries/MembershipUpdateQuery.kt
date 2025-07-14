package org.kevin.trello_v2.tasks.mapper.queries

import org.kevin.trello_v2.tasks.model.MembershipRole

data class MembershipUpdateQuery(
    val boardId: String,
    val userUid: String,
    val role: MembershipRole? = null,
    val starred: Boolean? = null,
    val active: Boolean? = null,
)

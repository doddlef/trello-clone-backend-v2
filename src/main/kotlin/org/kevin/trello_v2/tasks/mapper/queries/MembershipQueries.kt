package org.kevin.trello_v2.tasks.mapper.queries

import org.kevin.trello_v2.tasks.model.MembershipRole

data class MembershipInsertQuery (
    val boardId: String,
    val userUid: String,
    val role: MembershipRole,
    val starred: Boolean = false,
)

data class MembershipUpdateQuery(
    val boardId: String,
    val userUid: String,
    val role: MembershipRole? = null,
    val starred: Boolean? = null,
    val active: Boolean? = null,
)

data class MembershipSearchQuery(
    val userUid: String? = null,
    val boardId: String? = null,
    val isActive: Boolean? = true,
)
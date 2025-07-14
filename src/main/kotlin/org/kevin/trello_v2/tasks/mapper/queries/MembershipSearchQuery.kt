package org.kevin.trello_v2.tasks.mapper.queries

data class MembershipSearchQuery(
    val userUid: String? = null,
    val boardId: String? = null,
    val isActive: Boolean? = true,
)

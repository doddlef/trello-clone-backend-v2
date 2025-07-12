package org.kevin.trello_v2.tasks.repo

import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipSearchQuery
import org.kevin.trello_v2.tasks.model.BoardMembership

interface MemberRepo {
    fun findByKey(
        userUid: String,
        boardId: String
    ): BoardMembership?
    fun search(query: MembershipSearchQuery): List<BoardMembership>
    fun insert(query: MembershipInsertQuery): Int
}
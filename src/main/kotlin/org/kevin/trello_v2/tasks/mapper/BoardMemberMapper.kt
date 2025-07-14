package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipUpdateQuery
import org.kevin.trello_v2.tasks.model.BoardMembership

@Mapper
interface BoardMemberMapper {
    fun findByKey(
        @Param("userUid") userUid: String,
        @Param("boardId") boardId: String
    ): BoardMembership?
    fun search(query: MembershipSearchQuery): List<BoardMembership>
    fun insert(query: MembershipInsertQuery): Int
    fun update(query: MembershipUpdateQuery): Int
}
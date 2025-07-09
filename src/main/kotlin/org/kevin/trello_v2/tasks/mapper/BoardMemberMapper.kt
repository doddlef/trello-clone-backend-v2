package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery

@Mapper
interface BoardMemberMapper {
    fun insert(query: MembershipInsertQuery): Int
}
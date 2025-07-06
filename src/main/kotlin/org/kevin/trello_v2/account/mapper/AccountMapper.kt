package org.kevin.trello_v2.account.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.account.mapper.query.AccountInsertQuery

@Mapper
interface AccountMapper {
    fun insertAccount(query: AccountInsertQuery): Int
}
package org.kevin.trello_v2.account.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountUpdateQuery
import org.kevin.trello_v2.account.model.Account

@Mapper
interface AccountMapper {
    fun findByEmail(email: String): Account?
    fun findByUid(uid: String): Account?
    fun insertAccount(query: AccountInsertQuery): Int
    fun updateAccount(query: AccountUpdateQuery): Int
}
package org.kevin.trello_v2.account.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.account.model.Account

@Mapper
interface AccountMapper {
    fun findByEmail(email: String): Account?
    fun findByUid(uid: String): Account?

    /**
     * Insert a new account into the database.
     *
     * when update the database, the `AccountRepo` is recommended to use, to ensure the
     * cache is updated correctly.
     */
    fun insertAccount(query: AccountInsertQuery): Int

    /**
     * Update an existing account in the database.
     *
     * when update the database, the `AccountRepo` is recommended to use, to ensure the
     * cache is updated correctly.
     */
    fun updateAccount(query: AccountUpdateQuery): Int
}
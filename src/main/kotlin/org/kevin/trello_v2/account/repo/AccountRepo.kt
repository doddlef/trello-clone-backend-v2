package org.kevin.trello_v2.account.repo

import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountUpdateQuery
import org.kevin.trello_v2.account.model.Account

/**
 * Repository interface for managing accounts, combining both cache and database operations.
 */
interface AccountRepo {
    /**
     * Finds an account by its unique identifier (UID) from cache, or from database if not exist.
     *
     * @param uid The unique identifier of the account.
     * @return The account if found, or null if not found.
     */
    fun findByUid(uid: String): Account?

    /**
     * Finds an account by its email address.
     *
     * @param email The email address of the account.
     * @return The account if found, or null if not found.
     */
    fun findByEmail(email: String): Account?

    /**
     * Inserts a new account into the database.
     *
     * @param query The query containing the account details to be inserted.
     * @return The number of rows affected by the insert operation.
     */
    fun insertAccount(query: AccountInsertQuery): Int

    /**
     * Updates an existing account in the database, if success, it will evict the account from cache.
     *
     * @param query The query containing the account details to be updated.
     * @return The number of rows affected by the update operation.
     */
    fun updateAccount(query: AccountUpdateQuery): Int
}
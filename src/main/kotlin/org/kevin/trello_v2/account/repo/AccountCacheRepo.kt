package org.kevin.trello_v2.account.repo

import org.kevin.trello_v2.account.model.Account

interface AccountCacheRepo {
    /**
     * Finds an account by its unique identifier (UID).
     *
     * @param uid The unique identifier of the account.
     * @return The account if found, or null if not found.
     */
    fun find(uid: String): Account?

    /**
     * Evicts the account from the cache using its unique identifier (UID).
     */
    fun evict(uid: String)

    /**
     * Saves the account to the cache.
     *
     * @param account The account to be saved.
     * @return The saved account.
     */
    fun save(account: Account): Account
}
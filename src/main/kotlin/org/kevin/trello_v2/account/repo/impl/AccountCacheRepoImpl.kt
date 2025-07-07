package org.kevin.trello_v2.account.repo.impl

import org.kevin.trello_v2.account.mapper.AccountMapper
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.account.repo.AccountCacheRepo
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@Repository
class AccountCacheRepoImpl(
    private val accountMapper: AccountMapper,
): AccountCacheRepo {
    @Cacheable("accounts", key = "#uid")
    override fun find(uid: String): Account? {
        return accountMapper.findByUid(uid)
    }

    @CacheEvict("accounts", key = "#uid")
    override fun evict(uid: String) {
    }

    @CachePut("accounts", key = "#account.uid")
    override fun save(account: Account): Account {
        return account
    }
}
package org.kevin.trello_v2.account.repo.impl

import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountMapper
import org.kevin.trello_v2.account.mapper.AccountUpdateQuery
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.account.repo.AccountCacheRepo
import org.kevin.trello_v2.account.repo.AccountRepo
import org.springframework.stereotype.Repository

@Repository
class AccountRepoImpl(
    private val accountMapper: AccountMapper,
    private val accountCacheRepo: AccountCacheRepo,
): AccountRepo {
    override fun findByUid(uid: String): Account? =
        accountCacheRepo.find(uid)

    override fun findByEmail(email: String): Account? =
        accountMapper
            .findByEmail(email)
            ?.let {
                accountCacheRepo.save(it)
                it
            }

    override fun insertAccount(query: AccountInsertQuery): Int =
        accountMapper.insertAccount(query)

    override fun updateAccount(query: AccountUpdateQuery): Int =
        accountMapper
            .updateAccount(query)
            .let {
                if (it == 1) accountCacheRepo.evict(query.uid)
                it
            }

}
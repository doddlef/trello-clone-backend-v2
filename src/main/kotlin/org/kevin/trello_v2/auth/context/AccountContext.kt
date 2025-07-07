package org.kevin.trello_v2.auth.context

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.auth.model.AccountDetailAdaptor
import org.kevin.trello_v2.framework.TrelloException
import org.springframework.security.core.context.SecurityContextHolder

object AccountContext {
    fun currentAccount(): Account? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }
        return (authentication.principal as? AccountDetailAdaptor)?.principal
    }

    fun currentAccountOrThrow(): Account {
        return currentAccount() ?: throw TrelloException("Haven't logged in, please login first")
    }
}
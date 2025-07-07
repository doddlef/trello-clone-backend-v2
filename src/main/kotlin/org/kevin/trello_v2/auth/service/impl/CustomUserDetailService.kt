package org.kevin.trello_v2.auth.service.impl

import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.auth.exception.EmailNotVerifiedException
import org.kevin.trello_v2.auth.model.AccountDetailAdaptor
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val accountRepo: AccountRepo,
): UserDetailsService {

    /**
     * load user by username(email).
     */
    override fun loadUserByUsername(username: String?): UserDetails {
        if (username.isNullOrBlank()) throw UsernameNotFoundException("email cannot be null or blank")
        val account = accountRepo.findByEmail(username)

        if (account == null) throw UsernameNotFoundException("email $username not found")
        if (!account.verified) throw EmailNotVerifiedException(account.email)
        return AccountDetailAdaptor(account)
    }
}
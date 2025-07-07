package org.kevin.trello_v2.auth.model

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.account.model.AccountStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AccountDetailAdaptor(
    val principal: Account,
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return listOf(SimpleGrantedAuthority("ROLE_" + principal.role))
    }

    override fun getPassword(): String? {
        return principal.password
    }

    override fun getUsername(): String {
        return principal.email
    }

    override fun isEnabled(): Boolean {
        return principal.verified
    }

    override fun isAccountNonLocked(): Boolean {
        return principal.status == AccountStatus.ACTIVE
    }
}

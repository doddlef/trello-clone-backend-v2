package org.kevin.trello_v2.account.mapper.query

import com.github.f4b6a3.ulid.UlidCreator
import org.kevin.trello_v2.account.model.AccountRole

data class AccountInsertQuery(
    val email: String,
    val password: String? = null,
    val name: String,
    val avatar: String? = null,
    val verified: Boolean = false,
    val role: AccountRole = AccountRole.USER,
) {
    val uid = UlidCreator.getMonotonicUlid().toString()
}

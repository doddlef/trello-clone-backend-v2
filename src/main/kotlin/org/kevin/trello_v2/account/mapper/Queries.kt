package org.kevin.trello_v2.account.mapper

import com.github.f4b6a3.ulid.UlidCreator
import org.kevin.trello_v2.account.model.AccountRole
import org.kevin.trello_v2.account.model.AccountStatus

data class AccountInsertQuery(
    val email: String,
    val password: String? = null,
    val nickname: String,
    val avatar: String? = null,
    val verified: Boolean = false,
    val role: AccountRole = AccountRole.USER,
) {
    val uid = UlidCreator.getMonotonicUlid().toString()
}

data class AccountUpdateQuery(
    val uid: String,
    val email: String? = null,
    val password: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val verified: Boolean? = null,
    val role: AccountRole? = null,
    val status: AccountStatus? = null,
)
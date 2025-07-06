package org.kevin.trello_v2.account.model

import java.sql.Timestamp

data class Account(
    val uid: String,
    val email: String,
    val password: String?,
    val name: String,
    val avatar: String?,
    val verified: Boolean,
    val role: AccountRole,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val archived: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

    override fun toString(): String {
        return "Account(uid='$uid', email='$email', password=$password, name='$name', verified=$verified, role=$role, archived=$archived)"
    }

    fun toDto() = AccountDto(
        uid = uid,
        name = name,
        avatar = avatar,
        role = role
    )
}

data class AccountDto(
    val uid: String,
    val name: String,
    val avatar: String? = null,
    val role: AccountRole,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountDto

        return uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}
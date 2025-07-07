package org.kevin.trello_v2.account.model

enum class AccountStatus {
    ACTIVE, LOCKED, ARCHIVED;

    companion object {
        fun fromString(value: String): AccountStatus {
            return when (value.uppercase()) {
                "ACTIVE" -> ACTIVE
                "LOCKED" -> LOCKED
                "ARCHIVED" -> ARCHIVED
                else -> throw IllegalArgumentException("Unknown AccountStatus: $value")
            }
        }
    }
}
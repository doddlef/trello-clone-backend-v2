package org.kevin.trello_v2.account.model

enum class AccountRole {
    /**
     * Admin role with full permissions, can manage users and settings.
     */
    ADMIN,

    /**
     * Regular user role with limited permissions
     */
    USER;

    companion object {
        fun fromString(value: String): AccountRole {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> throw IllegalArgumentException("Unknown AccountRole: $value")
            }
        }
    }
}
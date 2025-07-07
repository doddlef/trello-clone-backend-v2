package org.kevin.trello_v2.account.model

enum class AccountRole(
    val description: String,
) {
    /**
     * Admin role with full permissions, can manage users and settings.
     */
    ADMIN("ADMIN"),

    /**
     * Regular user role with limited permissions
     */
    USER("USER");

    companion object {
        fun fromString(value: String): AccountRole {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "USER" -> USER
                else -> throw IllegalArgumentException("Unknown AccountRole: $value")
            }
        }
    }

    override fun toString(): String {
        return "AccountRole(description='$description')"
    }
}
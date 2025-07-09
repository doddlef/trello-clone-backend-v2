package org.kevin.trello_v2.tasks.model

import java.time.LocalDateTime

enum class MembershipRole {
    ADMIN, MEMBER, VIEWER;

    companion object {
        fun fromString(role: String): MembershipRole {
            return when (role.uppercase()) {
                "ADMIN" -> ADMIN
                "MEMBER" -> MEMBER
                "VIEWER" -> VIEWER
                else -> throw IllegalArgumentException("Unknown role: $role")
            }
        }
    }
}

data class BoardMembership(
    val boardId: String,
    val userUid: String,
    val role: MembershipRole,
    val starred: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val active: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardMembership

        if (boardId != other.boardId) return false
        if (userUid != other.userUid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boardId.hashCode()
        result = 31 * result + userUid.hashCode()
        return result
    }

    fun asDto(): MembershipDto {
        return MembershipDto(
            boardId = boardId,
            userUid = userUid,
            role = role,
            starred = starred,
        )
    }
}

data class MembershipDto(
    val boardId: String,
    val userUid: String,
    val role: MembershipRole,
    val starred: Boolean,
)
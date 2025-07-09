package org.kevin.trello_v2.tasks.model

import java.time.LocalDateTime

enum class BoardVisibility {
    PUBLIC, PRIVATE;

    companion object {
        fun fromString(value: String): BoardVisibility {
            return when (value.uppercase()) {
                "PUBLIC" -> PUBLIC
                "PRIVATE" -> PRIVATE
                else -> throw IllegalArgumentException("Unknown visibility: $value")
            }
        }
    }
}

data class Board(
    val id: String,
    val title: String,
    val description: String?,
    val visibility: BoardVisibility,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String,
    val archived: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Board

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun asDto(): BoardDto {
        return BoardDto(
            id = id,
            title = title,
            description = description,
            visibility = visibility,
        )
    }
}

data class BoardDto(
    val id: String,
    val title: String,
    val description: String?,
    val visibility: BoardVisibility,
)

//data class BoardView(
//    val boardId: String,
//    val userUid: String,
//    val title: String,
//    val description: String?,
//    val visibility: BoardVisibility,
//    val role: MembershipRole,
//    val starred: Boolean,
//) {
//    constructor(board: BoardDto, membership: MembershipDto): this(
//        boardId = board.id,
//        userUid = membership.userUid,
//        title = board.title,
//        description = board.description,
//        visibility = board.visibility,
//        role = membership.role,
//        starred = membership.starred
//    )
//
//    constructor(board: Board, membership: BoardMembership): this(
//        board.asDto(), membership.asDto()
//    )
//}
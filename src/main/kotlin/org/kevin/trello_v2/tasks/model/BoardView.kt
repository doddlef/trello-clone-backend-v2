package org.kevin.trello_v2.tasks.model

/**
 * represents a view of a board for the user.
 */
data class BoardView(
    val boardId: String,
    val title: String,
    val description: String?,
    val closed: Boolean,
    val userUid: String,
    val role: MembershipRole,
    val starred: Boolean,
) {
    constructor(board: BoardDto, membership: MembershipDto) : this(
        boardId = board.id,
        userUid = membership.userUid,
        title = board.title,
        description = board.description,
        closed = board.closed,
        role = membership.role,
        starred = membership.starred
    )

    constructor(board: Board, membership: BoardMembership) : this(board.asDto(), membership.asDto())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardView

        if (boardId != other.boardId) return false
        if (userUid != other.userUid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boardId.hashCode()
        result = 31 * result + userUid.hashCode()
        return result
    }
}
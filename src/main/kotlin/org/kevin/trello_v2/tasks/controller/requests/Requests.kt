package org.kevin.trello_v2.tasks.controller.requests

import java.time.LocalDateTime

data class CreateBoardRequest(
    val title: String,
    val description: String?
)

data class UpdateBoardRequest(
    val title: String?,
    val description: String?,
)

data class InviteMemberRequest(
    val guestUid: String,
    /* ADMIN, MEMBER, VIEWER */
    val role: String = "VIEWER",
)

data class UpdateMemberRequest(
    val role: String,
)

data class CreateListRequest(
    val title: String,
)

/**
 * @param title: The new title of the list. If null, the title will not be updated.
 * @param color: The new color of the list. If null, the color will not be updated.
 * @param clearColor: If true, the color will be set to null.
 */
data class UpdateListRequest(
    val title: String? = null,
    val color: String? = null,
    val clearColor: Boolean? = null,
)

data class MoveListRequest(
    val afterId: Long? = null,
)

data class CreateCardRequest(
    val title: String,
)

data class EditCardRequest(
    val title: String? = null,
    val description: String? = null,
    val dueDate: String? = null,
)
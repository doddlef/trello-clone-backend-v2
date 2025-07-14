package org.kevin.trello_v2.tasks.controller.requests

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
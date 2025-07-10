package org.kevin.trello_v2.tasks.controller.requests

data class CreateBoardRequest(
    val title: String,
    val description: String?
)
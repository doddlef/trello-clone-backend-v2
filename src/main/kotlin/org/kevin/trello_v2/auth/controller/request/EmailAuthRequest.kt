package org.kevin.trello_v2.auth.controller.request

data class EmailAuthRequest(
    val email: String,
    val password: String,
)
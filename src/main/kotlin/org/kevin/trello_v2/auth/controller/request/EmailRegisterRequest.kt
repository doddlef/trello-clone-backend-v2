package org.kevin.trello_v2.auth.controller.request

data class EmailRegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
)

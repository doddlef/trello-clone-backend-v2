package org.kevin.trello_v2.auth.service.vo

data class EmailPasswordAuthVo(
    /**
     * Email address of the user.
     */
    val email: String,

    /**
     * Plain password of the user.
     */
    val password: String
)

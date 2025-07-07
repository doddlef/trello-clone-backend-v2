package org.kevin.trello_v2.auth.service.vo

data class EmailRegisterVo(
    /**
     * The email address of the user.
     */
    val email: String,

    /**
     * The password for the user account.
     */
    val password: String,
    
    /**
     * The nickname for the user account.
     */
    val nickname: String
)

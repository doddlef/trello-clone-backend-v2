package org.kevin.trello_v2.auth.service

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.auth.service.vo.EmailRegisterVo
import org.kevin.trello_v2.framework.response.ApiResponse

interface RegisterService {
    /**
     * Registers a new user with the provided email and password.
     * If successful, an email verification will be sent to the user.
     * The created account should not be verified until the user verifies their email address.
     */
    fun emailRegister(vo: EmailRegisterVo): ApiResponse

    /**
     * Verifies the user's email address using the provided token.
     * If the token is valid, the user's account will be activated.
     *
     * @param content The content of verification token sent to the user's email.
     * @return the account if the verification is successful.
     */
    fun verificationEmail(content: String): Account

    /**
     * Resends the verification email to the user.
     *
     * @param uid The unique identifier of the user to whom the verification email should be resent.
     * @return An ApiResponse indicating the success or failure of the operation.
     */
    fun resendVerificationEmail(uid: String): ApiResponse

    /**
     * Cleans up expired email verification tokens.
     * This method should be called periodically to remove tokens that are no longer valid.
     */
    fun cleanUpExpiredTokens()
}
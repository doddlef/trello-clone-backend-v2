package org.kevin.trello_v2.framework.response

enum class ResponseCode(
    val code: Int,
) {
    /**
     * Success response code.
     */
    SUCCESS(0),

    /**
     * Error response code.
     */
    ERROR(1),

    /**
     * Business error response code.
     */
    BUSINESS_ERROR(1001),

    /**
     * Bad argument response code.
     */
    BAD_ARGUMENT(1002),

    /**
     * Access denied response code.
     */
    ACCESS_DENIED(2),

    /**
     * Access token expired response code.
     */
    TOKEN_EXPIRED(2001),

    /**
     * Bad credentials response code.
     */
    BAD_CREDENTIALS(2002),

    /**
     * Email not verified response code.
     */
    EMAIL_NOT_VERIFIED(2003),

    /**
     * need to login response code.
     */
    NEED_LOGIN(2010),
}
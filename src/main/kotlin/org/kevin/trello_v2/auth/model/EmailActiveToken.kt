package org.kevin.trello_v2.auth.model

import java.time.LocalDateTime

/**
 * Represents an email activation token used for verifying email addresses.
 *
 * @property content The content of the activation token.
 * @property uid The unique identifier of the user associated with the token.
 * @property expiredAt The date and time when the token expires.
 */
data class EmailActiveToken(
    val content: String,
    val uid: String,
    val expiredAt: LocalDateTime,
)

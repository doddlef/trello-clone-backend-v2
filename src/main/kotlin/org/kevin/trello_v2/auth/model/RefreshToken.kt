package org.kevin.trello_v2.auth.model

import java.time.LocalDateTime

/**
 * Represents a refresh token used for refreshing access tokens.
 */
data class RefreshToken(
    val content: String,
    val uid: String,
    val expireAt: LocalDateTime,
)

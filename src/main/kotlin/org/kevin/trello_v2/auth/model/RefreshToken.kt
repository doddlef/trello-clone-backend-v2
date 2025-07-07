package org.kevin.trello_v2.auth.model

import org.kevin.trello_v2.framework.utils.TokenGenerator
import java.time.LocalDateTime

/**
 * Represents a refresh token used for refreshing access tokens.
 */
data class RefreshToken(
    val content: String = TokenGenerator.generateToken(48),
    val accountUid: String,
    val expireAt: LocalDateTime,
)

package org.kevin.trello_v2.auth.exception

import org.kevin.trello_v2.framework.TrelloException

class InvalidRefreshTokenException(
    message: String = "Invalid refresh token"
): TrelloException(message)
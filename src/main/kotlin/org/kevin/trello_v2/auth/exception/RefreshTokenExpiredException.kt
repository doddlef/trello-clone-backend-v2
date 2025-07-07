package org.kevin.trello_v2.auth.exception

import org.kevin.trello_v2.framework.TrelloException

class RefreshTokenExpiredException: TrelloException("token has expired") {
    companion object {
        private const val serialVersionUID = 1L
    }
}
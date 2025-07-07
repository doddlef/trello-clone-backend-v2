package org.kevin.trello_v2.auth.exception

import org.kevin.trello_v2.framework.TrelloException

class EmailNotVerifiedException(
    val email: String,
): TrelloException("Email not verified") {
    companion object {
        private const val serialVersionUID = 1L
    }
}
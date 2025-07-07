package org.kevin.trello_v2.framework.utils

import java.security.SecureRandom
import java.util.Base64

object TokenGenerator {
    fun generateToken(size: Int = 32): String {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
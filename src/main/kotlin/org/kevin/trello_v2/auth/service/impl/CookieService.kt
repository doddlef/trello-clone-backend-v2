package org.kevin.trello_v2.auth.service.impl

import jakarta.servlet.http.HttpServletRequest
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.auth.AuthProperties
import org.kevin.trello_v2.auth.component.JwtUtils
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.web.util.WebUtils
import java.util.Date

@Service
class CookieService(
    private val authProperties: AuthProperties,
    private val jwtUtils: JwtUtils,
) {
    fun generateAccessCookie(account: Account): ResponseCookie {
        val token = jwtUtils.generateToken(
            account.uid,
            Date(System.currentTimeMillis() + authProperties.accessTokenLifeMinutes * 60 * 1000),
            mapOf("name" to account.nickname)
        )
        return ResponseCookie.from(authProperties.accessCookieName, token)
            .path("/")
            .maxAge(authProperties.refreshTokenLifeDays * 24 * 60 * 60L + 60) // trigger refresh after expired
            .httpOnly(true)
            .build()
    }

    fun extractAccessToken(request: HttpServletRequest) =
        getCookieValueByName(request, authProperties.accessCookieName)

    fun generateCleanAccessCookie() =
        ResponseCookie.from(authProperties.accessCookieName)
            .maxAge(0)
            .path("/")
            .httpOnly(true)
            .build()

    fun generateRefreshCookie(token: String): ResponseCookie =
        ResponseCookie.from(authProperties.refreshCookieName, token)
            .path("/api/auth/refresh")
            .maxAge(authProperties.refreshTokenLifeDays * 24 * 60 * 60L + 60)
            .httpOnly(true)
            .build()

    fun extractRefreshToken(request: HttpServletRequest): String? =
        getCookieValueByName(request, authProperties.refreshCookieName)

    fun generateCleanRefreshCookie() =
        ResponseCookie.from(authProperties.refreshCookieName)
            .maxAge(0)
            .path("/api/auth/refresh")
            .httpOnly(true)
            .build()

    private fun getCookieValueByName(request: HttpServletRequest, name: String): String? =
        WebUtils.getCookie(request, name)?.value
}
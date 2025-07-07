package org.kevin.trello_v2.auth.component

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.auth.model.AccountDetailAdaptor
import org.kevin.trello_v2.auth.service.impl.CookieService
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.response.ResponseCode
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

val JWT_AUTH_WHITE_LIST = listOf(
    "/api/auth/**",
    "/static/**",
    "/assets/**",
)

@Component
class JwtAuthFilter(
    private val jwtUtils: JwtUtils,
    private val cookieService: CookieService,
    private val accountRepo: AccountRepo,
    private val objectMapper: ObjectMapper,
): OncePerRequestFilter() {
    private val pathMather = AntPathMatcher()
    private fun isWhiteListed(path: String) =
        JWT_AUTH_WHITE_LIST.any {
            pathMather.match(it, path)
        }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (isWhiteListed(request.servletPath)) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwt = cookieService.extractAccessToken(request)
            if (jwt.isNullOrBlank()) {
                filterChain.doFilter(request, response)
                return
            }

            val uid = jwtUtils.extractSubject(jwt)
            if (uid != null && SecurityContextHolder.getContext().authentication == null) {
                val optional = accountRepo.findByUid(uid)

                if (optional != null && jwtUtils.isTokenValid(jwt, optional.uid)) {
                    val adaptor = AccountDetailAdaptor(optional)
                    val authenticationToken = UsernamePasswordAuthenticationToken(
                        adaptor,
                        null,
                        adaptor.authorities
                    )

                    authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authenticationToken
                }

                filterChain.doFilter(request, response)
            }
        } catch (_: ExpiredJwtException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            val body = ApiResponse.Builder(ResponseCode.TOKEN_EXPIRED)
                .message("Token expired")
                .build()

            response.writer.write(objectMapper.writeValueAsString(body))
            response.writer.flush()
        } catch (_: Exception) {
            filterChain.doFilter(request, response)
        }
    }
}
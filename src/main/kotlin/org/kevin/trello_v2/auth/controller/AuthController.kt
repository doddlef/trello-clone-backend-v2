package org.kevin.trello_v2.auth.controller

import jakarta.servlet.http.HttpServletRequest
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.auth.controller.request.EmailActiveRequest
import org.kevin.trello_v2.auth.controller.request.EmailAuthRequest
import org.kevin.trello_v2.auth.controller.request.EmailRegisterRequest
import org.kevin.trello_v2.auth.exception.InvalidRefreshTokenException
import org.kevin.trello_v2.auth.model.AccountDetailAdaptor
import org.kevin.trello_v2.auth.service.AuthService
import org.kevin.trello_v2.auth.service.RefreshService
import org.kevin.trello_v2.auth.service.RegisterService
import org.kevin.trello_v2.auth.service.impl.CookieService
import org.kevin.trello_v2.auth.service.vo.EmailPasswordAuthVo
import org.kevin.trello_v2.auth.service.vo.EmailRegisterVo
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val registerService: RegisterService,
    private val cookieService: CookieService,
    private val refreshService: RefreshService,
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping
    fun emailPasswordLogin(@RequestBody request: EmailAuthRequest): ResponseEntity<ApiResponse> {
        val (email, password) = request
        val authentication = authService.emailPasswordAuthenticate(EmailPasswordAuthVo(email, password))

        SecurityContextHolder.getContext().authentication = authentication

        return generateAuthedResponse((authentication.principal as AccountDetailAdaptor).principal)
    }

    @PostMapping("/register")
    fun emailPasswordRegister(@RequestBody request: EmailRegisterRequest): ApiResponse {
        val (email, password, nickname) = request
        return registerService.emailRegister(EmailRegisterVo(email, password, nickname))
    }

    @PutMapping("/active-email")
    fun activeEmail(@RequestBody request: EmailActiveRequest): ResponseEntity<ApiResponse> {
        val account = registerService.verificationEmail(request.token)
        return generateAuthedResponse(account)
    }

    @GetMapping("/resend-email-token")
    fun resendToken(@RequestParam("email") email: String): ApiResponse {
        return registerService.resendVerificationEmail(email)
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<ApiResponse> {
        cookieService.extractAccessToken(request)?.let {
            refreshService.deleteToken(it)
        }

        val refreshCookie = cookieService.generateCleanRefreshCookie()
        val accessCookie = cookieService.generateCleanAccessCookie()

        return ResponseEntity.ok()
            .headers {
                it.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                it.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            }
            .body(ApiResponse.success()
                .message("logout success")
                .build()
            )
    }

    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest): ResponseEntity<ApiResponse> {
        val refreshToken = cookieService.extractRefreshToken(request)

        if (!refreshToken.isNullOrBlank()) {
            val token = (refreshService.findByTokenContent(refreshToken)
                ?: throw InvalidRefreshTokenException("token does not exist"))

            return refreshService.verifyToken(token)
                .let(refreshService::getAccountByToken)
                .let {
                    val accessCookie = cookieService.generateAccessCookie(it)
                    val response = ApiResponse.success()
                        .message("token is refreshed")
                        .build()
                    val responseEntity = ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .body(response)
                    responseEntity
                }
        }

        log.debug("refresh token is empty")
        val build = ApiResponse.Builder(ResponseCode.ACCESS_DENIED)
            .message("refresh token is empty")
            .build()
        return ResponseEntity.badRequest().body(build)
    }

    private fun generateAuthedResponse(account: Account): ResponseEntity<ApiResponse> {
        val accessCookie = cookieService.generateAccessCookie(account)
        val refreshCookie = refreshService.createToken(account).let {
            cookieService.generateRefreshCookie(it.content)
        }

        val response = ApiResponse.success()
            .message("authenticate success")
            .add("account" to account.toDto())
            .build()

        return ResponseEntity.ok()
            .headers {
                it.add(HttpHeaders.SET_COOKIE, accessCookie.toString())
                it.add(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            }
            .body(response)
    }
}
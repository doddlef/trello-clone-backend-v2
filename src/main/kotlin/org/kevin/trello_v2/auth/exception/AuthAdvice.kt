package org.kevin.trello_v2.auth.exception

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["org.kevin.trello_v2.auth"])
class AuthAdvice {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(UsernameNotFoundException::class, BadCredentialsException::class)
    fun handleAuthException(e: Exception): ResponseEntity<ApiResponse> {
        log.debug("Bad credentials {}", e.message)
        val response = ApiResponse.Builder(ResponseCode.BAD_CREDENTIALS)
            .message(e.message ?: "Bad credentials")
            .build()
        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(RefreshTokenExpiredException::class)
    fun handleRefreshTokenExpiredException(e: RefreshTokenExpiredException): ResponseEntity<ApiResponse> {
        log.debug("Refresh token expired {}", e.message)
        val response = ApiResponse.Builder(ResponseCode.TOKEN_EXPIRED)
            .message("refresh token has expired")
            .build()
        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun handleEmailNotVerifiedException(e: EmailNotVerifiedException): ResponseEntity<ApiResponse> {
        log.debug("Email not verified {}", e.message)
        val response = ApiResponse.Builder(ResponseCode.EMAIL_NOT_VERIFIED)
            .message(e.message ?: "Email has not been verified")
            .add("email" to e.email)
            .build()
        return ResponseEntity(response, HttpStatus.UNAUTHORIZED)
    }
}
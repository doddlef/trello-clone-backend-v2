package org.kevin.trello_v2.account.exception

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.response.ResponseCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(basePackages = ["org.kevin.trello.auth"])
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
}
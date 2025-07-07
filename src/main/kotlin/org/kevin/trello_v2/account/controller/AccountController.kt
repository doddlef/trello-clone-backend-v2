package org.kevin.trello_v2.account.controller

import org.kevin.trello_v2.auth.context.AccountContext
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.response.ResponseCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account")
class AccountController {
    @GetMapping("/me")
    fun getAccountInfo(): ResponseEntity<ApiResponse> {
        val currentAccount = AccountContext.currentAccount()
        if (currentAccount == null) {
            return ResponseEntity(
                ApiResponse.Builder(ResponseCode.NEED_LOGIN)
                    .message("have not logged in")
                    .build(),
                HttpStatus.UNAUTHORIZED
            )
        }

        return ResponseEntity(
            ApiResponse.success()
                .add("account" to currentAccount.toDto())
                .build(),
            HttpStatus.OK
        )
    }
}
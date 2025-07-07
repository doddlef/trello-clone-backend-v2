package org.kevin.trello_v2.auth.controller

import org.kevin.trello_v2.auth.service.RegisterService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenCleaner(
    private val registerService: RegisterService,
) {
    @Scheduled(cron = "0 0 * * * *")
    fun cleanUpExpiredTokens() {
        registerService.cleanUpExpiredTokens()
    }
}
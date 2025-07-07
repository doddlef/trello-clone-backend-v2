package org.kevin.trello_v2.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "auth")
class AuthProperties {
    var accessCookieName: String = "trello_access_v2"
    var accessTokenLifeMinutes: Long = 30
    var refreshCookieName: String = "trello_refresh_v2"
    var refreshTokenLifeDays: Long = 7

    lateinit var jwtSecret: String

    var emailActiveTokenLifeHours: Long = 24
}
package org.kevin.trello_v2.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cache")
class CacheProperties {
    var accountLifeMinutes: Long = 15L
    var accountMaxSize: Long = 128L

    var boardLifeMinutes: Long = 15L
    var boardMaxSize: Long = 128L

    var memberLifeMinutes: Long = 15L
    var memberMaxSize: Long = 128L
}
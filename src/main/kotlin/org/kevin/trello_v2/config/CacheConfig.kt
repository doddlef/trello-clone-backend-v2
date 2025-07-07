package org.kevin.trello_v2.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@ConfigurationPropertiesScan(basePackageClasses = [CacheProperties::class])
class CacheConfig(
    private val cacheProperties: CacheProperties,
) {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()

        val accountCache = CaffeineCache(
            "accounts", Caffeine.newBuilder()
                .maximumSize(cacheProperties.accountMaxSize)
                .expireAfterAccess(cacheProperties.accountLifeMinutes, TimeUnit.MINUTES)
                .build()
        )

        cacheManager.setCaches(listOf(
            accountCache,
        ))

        return cacheManager
    }
}
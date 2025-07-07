package org.kevin.trello_v2.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    fun corsConfigurationSource() =
        UrlBasedCorsConfigurationSource().let {
            val configuration = CorsConfiguration().apply {
                this.addAllowedOrigin("http://localhost:5173")
                this.addAllowedMethod("*")
                this.addAllowedHeader("*")
                this.allowCredentials = true
            }
            it.registerCorsConfiguration("/**", configuration)
            it
        }

    @Bean
    fun authenticationManager (authConfig: AuthenticationConfiguration) =
        authConfig.authenticationManager

    @Bean
    fun passwordEncode() =
        BCryptPasswordEncoder()
}
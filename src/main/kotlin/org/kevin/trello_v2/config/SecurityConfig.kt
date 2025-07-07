package org.kevin.trello_v2.config

import org.kevin.trello_v2.auth.component.JwtAuthFilter
import org.kevin.trello_v2.auth.exception.AuthEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

val WHITE_LIST = arrayOf(
    "/api/auth/**",
    "/static/**",
    "/assets/**",
    "/api/account/me",
)

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val entryPoint: AuthEntryPoint
) {
    @Bean
    fun securityChain(http: HttpSecurity) =
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests {
                it
                    .requestMatchers(*WHITE_LIST).permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { it.authenticationEntryPoint(entryPoint) }
            .build()

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
package org.kevin.trello_v2.auth.service.impl

import org.kevin.trello_v2.auth.service.AuthService
import org.kevin.trello_v2.auth.service.vo.EmailPasswordAuthVo
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val authenticationManager: AuthenticationManager,
): AuthService {
    override fun emailPasswordAuthenticate(vo: EmailPasswordAuthVo): Authentication {
        return authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                vo.email,
                vo.password
            )
        )
    }
}
package org.kevin.trello_v2.auth.service

import org.kevin.trello_v2.auth.service.vo.EmailPasswordAuthVo
import org.springframework.security.core.Authentication

interface AuthService {
    fun emailPasswordAuthenticate(vo: EmailPasswordAuthVo): Authentication
}
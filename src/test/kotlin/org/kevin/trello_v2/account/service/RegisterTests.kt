package org.kevin.trello_v2.account.service

import org.junit.jupiter.api.Test
import org.kevin.trello_v2.auth.service.RegisterService
import org.kevin.trello_v2.auth.service.vo.EmailRegisterVo
import org.kevin.trello_v2.framework.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class RegisterTests @Autowired constructor(
    private val registerService: RegisterService,
) {
    @Test
    fun `register account`() {
        EmailRegisterVo(
            email = "test@gmail.com",
            password = "Test1234!",
            nickname = "testUser"
        )
            .let {
                val response = registerService.emailRegister(it)
                assertEquals(ResponseCode.SUCCESS.code, response.code)
            }
    }
}
package org.kevin.trello_v2.auth

import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountMapper
import org.kevin.trello_v2.framework.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.transaction.annotation.Transactional
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.cookies.CookieDocumentation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class AuthTests @Autowired constructor(
    private val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val accountMapper: AccountMapper,
    val passwordEncoder: PasswordEncoder,
) {
    @Test
    @DisplayName("auth flow: auth, refresh, logout")
    fun `auth, refresh, logout`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        val uid = AccountInsertQuery(
            email = email,
            nickname = nickname,
            password = passwordEncoder.encode(password),
            verified = true
        ).let {
            val count = accountMapper.insertAccount(it)
            assert(count == 1) { "Expected to insert one account, but inserted $count" }
            it.uid
        }

        // Auth
        val result = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
            .let {
                return@let mockMvc.perform(
                    post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.account").exists())
                    .andExpect(cookie().exists(authProperties.accessCookieName))
                    .andExpect(cookie().exists(authProperties.refreshCookieName))
                    .andDo(
                        document(
                            "email-password-auth",
                            requestFields(
                                fieldWithPath("email").description("Email of the user"),
                                fieldWithPath("password").description("Password of the user")
                            ),
                            responseCookies(
                                cookieWithName(authProperties.accessCookieName).description("Access token cookie"),
                                cookieWithName(authProperties.refreshCookieName).description("Refresh token cookie")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                                fieldWithPath("data.account.uid").description("UID of the authenticated account"),
                                fieldWithPath("data.account.nickname").description("Nickname of the authenticated account"),
                                fieldWithPath("data.account.avatar").optional().description("Avatar URL of the authenticated account"),
                                fieldWithPath("data.account.role").description("Role of the authenticated account")
                            )
                        )
                    )
                    .andReturn()
            }

        var accessCookie = result.response.getCookie(authProperties.accessCookieName)
        val refreshCookie = result.response.getCookie(authProperties.refreshCookieName)

        // Check account info
        mockMvc.perform(
            get("/api/account/me")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
            .andDo(
                document(
                    "get-account-info",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("Access token cookie")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("data.account.uid").description("UID of the authenticated account"),
                        fieldWithPath("data.account.nickname").description("Nickname of the authenticated account"),
                        fieldWithPath("data.account.avatar").optional().description("Avatar URL of the authenticated account"),
                        fieldWithPath("data.account.role").description("Role of the authenticated account")
                    )
                )
            )

        // refresh
        mockMvc.perform(
            post("/api/auth/refresh")
                .cookie(refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(cookie().exists(authProperties.accessCookieName))
            .andDo(
                document(
                    "refresh",
                    requestCookies(
                        cookieWithName(authProperties.refreshCookieName).description("refresh token cookie")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    ),
                    responseCookies(
                        cookieWithName(authProperties.accessCookieName).description("New access token cookie")
                    )
                )
            )
            .andReturn()
            .response
            .getCookie(authProperties.accessCookieName)
            .let {
                accessCookie = it
            }

        // logout
        mockMvc.perform(
            post("/api/auth/logout")
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(cookie().exists(authProperties.accessCookieName))
            .andExpect(cookie().exists(authProperties.refreshCookieName))
            .andDo(
                document(
                    "logout",
                    requestCookies(
                        cookieWithName(authProperties.accessCookieName).description("Access token cookie, should be cleared"),
                        cookieWithName(authProperties.refreshCookieName).description("Refresh token cookie, should be cleared")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    ),
                    responseCookies(
                        cookieWithName(authProperties.accessCookieName).description("cleaned access token cookie"),
                        cookieWithName(authProperties.refreshCookieName).description("cleaned refresh token cookie")
                    )
                )
            )
            .andReturn()
            .response
            .let {
                accessCookie = it.getCookie(authProperties.accessCookieName)
            }

        // recheck account info after logout
        mockMvc.perform(
            get("/api/account/me")
                .cookie(accessCookie)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ResponseCode.NEED_LOGIN.code))
            .andExpect(jsonPath("$.data.account").doesNotExist())
    }
}
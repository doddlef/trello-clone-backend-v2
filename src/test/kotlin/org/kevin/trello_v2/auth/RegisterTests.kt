package org.kevin.trello_v2.auth

import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello_v2.auth.mapper.EmailActiveTokenMapper
import org.kevin.trello_v2.framework.response.ResponseCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.MockMvc
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.cookies.CookieDocumentation.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
class RegisterTests @Autowired constructor(
    private val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val emailActiveTokenMapper: EmailActiveTokenMapper,
) {
    @Test
    fun `register flow`() {
        val email = "${RandomString(8).nextString()}@example.com"
        val nickname = "${RandomString(4).nextString()}-user"
        val password = "Password123!"

        """
            {
                "email": "$email",
                "nickname": "$nickname",
                "password": "$password"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.uid").exists())
                    .andDo(
                        document(
                            "register-using-email",
                            requestFields(
                                fieldWithPath("email").description("Email address for registration"),
                                fieldWithPath("nickname").description("Nickname for the account"),
                                fieldWithPath("password").description("Password for the account")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code, 0 for success"),
                                fieldWithPath("message").description("Response message, empty for success"),
                                fieldWithPath("data.uid").description("Unique identifier for the newly created account")
                            )
                        )
                    )
            }

        """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.data.email").exists())
                    .andDo(
                        document(
                            "auth-using-not-verified-email",
                            requestFields(
                                fieldWithPath("email").description("Email address for authentication"),
                                fieldWithPath("password").description("Password for authentication")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code, 401 for unauthorized"),
                                fieldWithPath("message").description("Response message, 'Email not verified'"),
                                fieldWithPath("data.email").description("Email address that needs verification")
                            )
                        )
                    )
            }

        // Testing only: retrieve the email active token
        val activeToken = emailActiveTokenMapper.findByEmail(email)
        assertNotNull(activeToken)
        val activeTokenContent = activeToken.content

        // Activate the account using the token
        val accessCookie = """
            {
                "token": "$activeTokenContent"
            }
        """.trimIndent()
            .let {
                return@let mockMvc.perform(
                    put("/api/auth/active-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.account").exists())
                    .andDo(
                        document(
                            "activate-email",
                            requestFields(
                                fieldWithPath("token").description("Email activation token")
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
                    .response
                    .getCookie(authProperties.accessCookieName)
            }

        // Check account info
        mockMvc.perform(
            get("/api/account/me")
                .cookie(accessCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.account").exists())
    }
}
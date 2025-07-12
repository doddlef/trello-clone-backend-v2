package org.kevin.trello_v2.tasks.web

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.auth.AuthProperties
import org.kevin.trello_v2.framework.response.ResponseCode
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class CacheTests @Autowired constructor(
    private val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountRepo: AccountRepo,
    val taskPathHelper: TaskPathHelper,
) {
    val email = "${RandomString(10)}@example.com"
    val password = "Password123!"
    val nickname = "test"
    lateinit var uid: String
    var accessCookie: Cookie? = null
    var refreshCookie: Cookie? = null

    @BeforeEach
    fun setUp() {
        AccountInsertQuery(
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            verified = true,
        ).let { query ->
            val count = accountRepo.insertAccount(query)
            assertEquals(1, count, "Account should be inserted successfully")
            uid = query.uid
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
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.account").exists())
                    .andExpect(cookie().exists(authProperties.accessCookieName))
                    .andExpect(cookie().exists(authProperties.refreshCookieName))
                    .andReturn()
                    .response
                    .let {
                        accessCookie = it.getCookie(authProperties.accessCookieName)
                        refreshCookie = it.getCookie(authProperties.refreshCookieName)
                    }
            }
    }

    @Test
    fun `test cache`() {
        val boardId = """
            {
                "title": "Test Board",
                "description": "This is a test board"
            }
        """.trimIndent()
            .let {
                return@let mockMvc.perform(
                    post("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.board").exists())
                    .andReturn()
                    .response
                    .contentAsString
                    .let {
                        return@let ObjectMapper().readTree(it)
                            .path("data")
                            .path("board")
                            .path("boardId")
                            .asText()
                    }
            }

        for (i in 1..10) {
            """
                {
                    "title": "$i",
                    "description": "${i} description"
                }
            """.trimIndent()
                .let {
                    mockMvc.perform(
                        put("/api/v1/board/{boardId}", boardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(it)
                            .cookie(accessCookie, refreshCookie)
                    )
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                }

            taskPathHelper.pathOfBoard(userUid = uid, boardId = boardId).let {
                val board = it.board
                assertNotNull(board)
                assertEquals("$i", board.title)
            }
        }
    }
}
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
import org.kevin.trello_v2.tasks.mapper.TaskListMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class ListTests @Autowired constructor(
    private val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountRepo: AccountRepo,
    val listMapper: TaskListMapper,
) {
    val email = "${RandomString(10)}@example.com"
    val password = "Password123!"
    val nickname = "test"
    lateinit var uid: String
    lateinit var boardId: String
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

        """
            {
                "title": "Test Board",
                "description": "This is a test board"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
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
                        boardId = ObjectMapper()
                            .readTree(it)
                            .get("data")
                            .get("board")
                            .path("boardId")
                            .asText()
                    }
            }
    }

    @Test
    fun `create list`() {
        """
            {
                "title": "Test List"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/board/{boardId}/list", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.listId").exists())
                    .andDo(
                        document(
                            "create-list",
                            pathParameters(
                                parameterWithName("boardId").description("The ID of the board to create the list in")
                            ),
                            requestFields(
                                fieldWithPath("title").description("The title of the list")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                                fieldWithPath("data.listId").description("Created list ID")
                            )
                        )
                    )
            }
    }

    @Test
    fun `update list`() {
        val t = "Test List"
        val listId = """
            {
                "title": "$t"
            }
        """.trimIndent()
            .let {
                return@let mockMvc.perform(
                    post("/api/v1/board/{boardId}/list", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.listId").exists())
                    .andReturn()
                    .response
                    .contentAsString
                    .let { str ->
                        ObjectMapper().readTree(str)
                            .path("data")
                            .path("listId")
                            .asLong()
                    }
            }

        val list = listMapper.findById(listId)
        assertNotNull(list)

        """
            {
                "title": "Updated List",
                "color": "#F0F0F0"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/list/{listId}", listId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andDo(
                        document(
                            "update-list",
                            pathParameters(
                                parameterWithName("listId").description("The ID of the list to update")
                            ),
                            requestFields(
                                fieldWithPath("title").optional().description("The new title of the list"),
                                fieldWithPath("color").optional().description("The new color of the list"),
                                fieldWithPath("clearColor").type(Boolean).optional().description("set true to clear the color of the list"),
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                            )
                        )
                    )
            }

        val updated = listMapper.findById(listId)
        assertNotNull(list)
        assertEquals("Updated List", updated?.title, "List title should be updated")
        assertEquals("#F0F0F0", updated?.color, "List color should be updated")
    }
}
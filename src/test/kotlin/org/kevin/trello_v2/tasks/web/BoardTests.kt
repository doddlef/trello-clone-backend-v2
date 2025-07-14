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
import org.springframework.transaction.annotation.Transactional
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class BoardTests @Autowired constructor(
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
    fun `create new board`() {
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
                    .andDo(
                        document(
                            "create-board",
                            requestFields(
                                fieldWithPath("title").description("Title of the board"),
                                fieldWithPath("description").optional().description("Description of the board"),
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                                fieldWithPath("data.board.boardId").description("ID of the created board"),
                                fieldWithPath("data.board.title").description("Title of the created board"),
                                fieldWithPath("data.board.description").type(String).optional().description("Description of the created board"),
                                fieldWithPath("data.board.closed").description("Whether the board is closed"),
                                fieldWithPath("data.board.userUid").description("User ID of the board creator"),
                                fieldWithPath("data.board.role").description("Role of the user in the board"),
                                fieldWithPath("data.board.starred").description("Whether the board is starred by the user")
                            )
                        )
                )
            }
    }

    @Test
    fun `update board`() {
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

        val newTitle = "New Title"
        val newDescription = "New Description"
        """
            {
                "title": "$newTitle",
                "description": "$newDescription"
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
                    .andDo(
                        document(
                            "update-board",
                            pathParameters(
                                parameterWithName("boardId").description("ID of the board to update")
                            ),
                            requestFields(
                                fieldWithPath("title").optional().description("New title of the board"),
                                fieldWithPath("description").optional().description("New description of the board")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                            )
                        )
                    )
            }
    }

    @Test
    fun `close, archive and list of boards`() {
        val boardIds = mutableListOf<String>()
        for (i in 1..3) {
            """
                {
                    "title": "Test Board $i",
                    "description": "This is a test board $i"
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
                        .andReturn()
                        .response
                        .contentAsString
                        .let {
                            val boardId = ObjectMapper().readTree(it)
                                .path("data")
                                .path("board")
                                .path("boardId")
                                .asText()
                            boardIds.add(boardId)
                        }
                }
        }

        // get the list of boards
        mockMvc.perform(
            get("/api/v1/board-list")
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.boards").isArray)
            .andDo(
                document(
                    "list-boards",
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("data.boards[]").description("List of boards"),
                        fieldWithPath("data.boards[].boardId").description("ID of the board"),
                        fieldWithPath("data.boards[].title").description("Title of the board"),
                        fieldWithPath("data.boards[].description").type(String).optional().description("Description of the board"),
                        fieldWithPath("data.boards[].closed").description("Whether the board is closed"),
                        fieldWithPath("data.boards[].userUid").description("User ID of the board creator"),
                        fieldWithPath("data.boards[].role").description("Role of the user in the board"),
                        fieldWithPath("data.boards[].starred").description("Whether the board is starred by the user")
                    )
                )
            )
            .andReturn()
            .response
            .contentAsString
            .let {
                val boards = ObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("boards")
                assertEquals(3, boards.size(), "There should be 3 boards in the list")
            }

        // close the first board
        mockMvc.perform(
            delete("/api/v1/board/{id}", boardIds[0])
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "close-board",
                    pathParameters(
                        parameterWithName("id").description("ID of the board to close")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )

        mockMvc.perform(
            get("/api/v1/board-list")
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.boards").isArray)
            .andReturn()
            .response
            .contentAsString
            .let {
                val boards = ObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("boards")
                assertEquals(3, boards.size(), "Still three boards should be listed")
            }

        taskPathHelper.pathOfBoard(uid, boardIds[0]).let {
            val board = it.boardView
            assertNotNull(board)
            assertTrue(board.closed)
        }

        // archive the second board
        mockMvc.perform(
            delete("/api/v1/board/{id}/archive", boardIds[0])
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "archive-board",
                    pathParameters(
                        parameterWithName("id").description("ID of the board to archive")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )

        mockMvc.perform(
            get("/api/v1/board-list")
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andExpect(jsonPath("$.data.boards").isArray)
            .andReturn()
            .response
            .contentAsString
            .let {
                val boards = ObjectMapper()
                    .readTree(it)
                    .path("data")
                    .path("boards")
                assertEquals(2, boards.size(), "Only two boards should be listed after archiving one")
            }

        taskPathHelper.pathOfBoard(uid, boardIds[0]).let {
            assertNull(it.boardView)
        }
    }

    @Test
    fun `invite member`() {
        val guestUid = AccountInsertQuery(
            email = "${RandomString(10)}@example.com",
            password = passwordEncoder.encode(password),
            nickname = "test_invite",
            verified = true,
        ).let { query ->
            val count = accountRepo.insertAccount(query)
            assertEquals(1, count, "Account should be inserted successfully")
            query.uid
        }

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

        taskPathHelper.pathOfBoard(guestUid, boardId).let {
            val membership = it.member
            assertNull(membership)
        }

        """
            {
                "guestUid": "$guestUid",
                "role": "MEMBER"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    post("/api/v1/board/{id}/invite", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andDo(
                        document(
                            "invite-member",
                            pathParameters(
                                parameterWithName("id").description("ID of the board to invite member to")
                            ),
                            requestFields(
                                fieldWithPath("guestUid").description("User ID of the guest to invite"),
                                fieldWithPath("role").description("Role of the guest in the board (ADMIN, MEMBER, VIEWER)")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message")
                            )
                        )
                )
            }

        taskPathHelper.pathOfBoard(guestUid, boardId).let {
            val membership = it.member
            assertNotNull(membership)
        }
    }
}
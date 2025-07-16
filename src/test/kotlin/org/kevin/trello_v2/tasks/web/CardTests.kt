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
import org.kevin.trello_v2.tasks.mapper.CardMapper
import org.kevin.trello_v2.tasks.mapper.queries.CardSearchQuery
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
@Transactional
class CardTests @Autowired constructor(
    private val mockMvc: MockMvc,
    val authProperties: AuthProperties,
    val passwordEncoder: PasswordEncoder,
    val accountRepo: AccountRepo,
    val cardMapper: CardMapper,
) {
    val email = "${RandomString(10)}@example.com"
    val password = "Password123!"
    val nickname = "test"
    lateinit var uid: String
    lateinit var boardId: String
    var listId: Long = -1L
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

        """
            {
                "title": "Test List"
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
                        listId = ObjectMapper().readTree(str)
                            .path("data")
                            .path("listId")
                            .asLong()

                    }
            }
    }

    @Test
    fun `create card`() {
        mockMvc.perform(
            post("/api/v1/list/{listId}/card", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "title": "New Card"
                        }
                    """.trimIndent()
                )
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andDo(
                document(
                    "create-card",
                    pathParameters(
                        parameterWithName("listId").description("The ID of the list to create the card in")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the card")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message"),
                        fieldWithPath("data.cardId").description("Card ID"),
                        fieldWithPath("data.position").description("Position of the card in the list")
                    )
                )
            )
    }

    @Test
    fun `update, complete and incomplete`() {
        val cardId = mockMvc.perform(
            post("/api/v1/list/{listId}/card", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {
                            "title": "New Card"
                        }
                    """.trimIndent()
                )
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andReturn()
            .response
            .contentAsString
            .let {
                ObjectMapper().readTree(it)
                    .path("data")
                    .path("cardId")
                    .asLong()
            }

        cardId.let {
            val card = cardMapper.findById(it)
            assertNotNull(card)
            assertEquals("New Card", card.title, "Card title should match")
            assertNull(card.description, "Description should be unset")
            assertNull(card.dueDate, "Due date should be unset")
            assertFalse(card.finished, "Finished should not be completed")
        }

        // edit card
        """
            {                
                "title": "Updated Card",
                "description": "This is an updated description",
                "dueDate": "2023-12-31"
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/card/{cardId}/edit", cardId)
                        .content(it)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andDo(
                        document(
                            "edit-card",
                            pathParameters(
                                parameterWithName("cardId").description("The ID of the card to edit")
                            ),
                            requestFields(
                                fieldWithPath("title").optional().description("The new title of the card"),
                                fieldWithPath("description").optional().description("The new description of the card"),
                                fieldWithPath("dueDate").optional().description("The new due date of the card in ISO format")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message")
                            )
                        )
                    )
            }

        cardId.let {
            val card = cardMapper.findById(it)
            assertNotNull(card)
            assertEquals("Updated Card", card.title, "Card title should be updated")
            assertEquals("This is an updated description", card.description, "Card description should be updated")
            assertEquals("2023-12-31", card.dueDate.toString(), "Card due date should be updated")
            assertFalse(card.finished, "Finished should still not be completed")
        }

        // complete card
        mockMvc.perform(
            put("/api/v1/card/{cardId}/complete", cardId)
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andDo(
                document(
                    "complete-card",
                    pathParameters(
                        parameterWithName("cardId").description("The ID of the card to complete")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )

        cardId.let {
            val card = cardMapper.findById(it)
            assertNotNull(card)
            assertTrue(card.finished, "card should be marked as completed")
        }

        // incomplete card
        mockMvc.perform(
            put("/api/v1/card/{cardId}/incomplete", cardId)
                .cookie(accessCookie, refreshCookie)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            .andDo(
                document(
                    "incomplete-card",
                    pathParameters(
                        parameterWithName("cardId").description("The ID of the card to mark as incomplete")
                    ),
                    responseFields(
                        fieldWithPath("code").description("Response code"),
                        fieldWithPath("message").description("Response message")
                    )
                )
            )

        cardId.let {
            val card = cardMapper.findById(it)
            assertNotNull(card)
            assertFalse(card.finished, "card should be marked as incomplete")
        }
    }

    @Test
    fun `card move`() {
        val cardIds = mutableListOf<Long>()
        repeat(5) { index ->
            mockMvc.perform(
                post("/api/v1/list/{listId}/card", listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {
                                "title": "Card $index"
                            }
                        """.trimIndent()
                    )
                    .cookie(accessCookie, refreshCookie)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                .andReturn()
                .response
                .contentAsString
                .let {
                    cardIds.add(ObjectMapper().readTree(it).path("data").path("cardId").asLong())
                }
        }

        var secondListId: Long
        """
            {
                "title": "Second List"
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
                        secondListId = ObjectMapper().readTree(str)
                            .path("data")
                            .path("listId")
                            .asLong()

                    }
            }

        CardSearchQuery(listId = listId)
            .let { cardMapper.search(it) }
            .sortedBy { it.position }
            .let { cards ->
                assertEquals(5, cards.size, "There should be 5 cards in the first list")
                assertEquals("Card 0", cards[0].title, "First card title should be 'Card 0'")
                assertEquals("Card 1", cards[1].title, "Second card title should be 'Card 1'")
                assertEquals("Card 2", cards[2].title, "Third card title should be 'Card 2'")
                assertEquals("Card 3", cards[3].title, "Fourth card title should be 'Card 3'")
                assertEquals("Card 4", cards[4].title, "Fifth card title should be 'Card 4'")
            }

        // move card 0 to tail
        """
            {
                "afterId": ${cardIds[4]}
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/card/{cardId}/move", cardIds[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            }

        CardSearchQuery(listId = listId)
            .let { cardMapper.search(it) }
            .sortedBy { it.position }
            .let { cards ->
                assertEquals(5, cards.size, "There should still be 5 cards in the first list")
                assertEquals("Card 1", cards[0].title, "First card title should now be 'Card 1'")
                assertEquals("Card 2", cards[1].title, "Second card title should now be 'Card 2'")
                assertEquals("Card 3", cards[2].title, "Third card title should now be 'Card 3'")
                assertEquals("Card 4", cards[3].title, "Fourth card title should now be 'Card 4'")
                assertEquals("Card 0", cards[4].title, "Fifth card title should now be 'Card 0'")
            }

        // move card 0 to second list
        """
            {
                "listId": $secondListId
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/card/{cardId}/move", cardIds[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
            }

        CardSearchQuery(listId = secondListId)
            .let { cardMapper.search(it) }
            .let { cards ->
                assertEquals(1, cards.size, "There should be 1 card in the second list")
                assertEquals("Card 0", cards[0].title, "Card title in second list should be 'Card 0'")
            }

        // move card 1 after card 0 in second list
        """
            {
                "afterId": ${cardIds[0]}
            }
        """.trimIndent()
            .let {
                mockMvc.perform(
                    put("/api/v1/card/{cardId}/move", cardIds[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(it)
                        .cookie(accessCookie, refreshCookie)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.code").value(ResponseCode.SUCCESS.code))
                    .andExpect(jsonPath("$.data.newPosition").exists())
                    .andExpect(jsonPath("$.data.newListId").value(secondListId))
                    .andDo(
                        document(
                            "move-card",
                            pathParameters(
                                parameterWithName("cardId").description("The ID of the card to move")
                            ),
                            requestFields(
                                fieldWithPath("listId").type(Long).optional().description("The ID of the list to move the card to. If unset, the card will be moved within the same list."),
                                fieldWithPath("afterId").type(Long).optional().description("The ID of the card after which to place this card. If unset, the card will be moved to the top of the list.")
                            ),
                            responseFields(
                                fieldWithPath("code").description("Response code"),
                                fieldWithPath("message").description("Response message"),
                                fieldWithPath("data.newPosition").description("Position of the card to place the card"),
                                fieldWithPath("data.newListId").description("ID of the list where the card was moved")
                            )
                        )
                    )
            }

        CardSearchQuery(listId = secondListId)
            .let { cardMapper.search(it) }
            .sortedBy { it.position }
            .let { cards ->
                assertEquals(2, cards.size, "There should be 2 cards in the second list")
                assertEquals("Card 0", cards[0].title, "First card title in second list should be 'Card 0'")
                assertEquals("Card 1", cards[1].title, "Second card title in second list should be 'Card 1'")
            }
    }
}
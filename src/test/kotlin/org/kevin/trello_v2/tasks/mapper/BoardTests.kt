package org.kevin.trello_v2.tasks.mapper

import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.repo.AccountRepo
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.model.BoardVisibility
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class BoardTests @Autowired constructor(
    val accountRepo: AccountRepo,
    val boardMapper: BoardMapper,
    val boardMemberMapper: BoardMemberMapper,
    val passwordEncoder: PasswordEncoder,
) {
    val email = "${RandomString(10)}@example.com"
    val password = "Password123!"
    val nickname = "test"
    lateinit var uid: String

    @BeforeEach
    fun setUp() {
        AccountInsertQuery(
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
        ).let { query ->
            val count = accountRepo.insertAccount(query)
            assertEquals(1, count, "Account should be inserted successfully")
            uid = query.uid
        }
    }

    @Test
    fun `insert board and membership`() {
        val boardId = BoardInsertQuery(
            title = "title",
            description = "description",
            visibility = BoardVisibility.PRIVATE,
            createdBy = uid,
        ).let { query ->
            val count = boardMapper.insert(query)
            assertEquals(1, count, "Board should be inserted successfully")
            query.id
        }

        MembershipInsertQuery(
            boardId = boardId,
            userUid = uid,
            role = MembershipRole.ADMIN,
        ).let { query ->
            val count = boardMemberMapper.insert(query)
            assertEquals(1, count, "Membership should be inserted successfully")
        }
    }
}
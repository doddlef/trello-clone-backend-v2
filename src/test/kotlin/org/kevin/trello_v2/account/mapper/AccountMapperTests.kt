package org.kevin.trello_v2.account.mapper

import org.junit.jupiter.api.Test
import org.kevin.trello_v2.account.mapper.query.AccountInsertQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class AccountMapperTests @Autowired constructor(
    val accountMapper: AccountMapper,
) {

    @Test
    fun `insert new account`() {
        AccountInsertQuery(
            email = "test@gmail.com",
            name = "test",
        )
            .let {
                val count = accountMapper.insertAccount(it)
                assertEquals(1, count, "Expected to insert one account")
            }
    }
}
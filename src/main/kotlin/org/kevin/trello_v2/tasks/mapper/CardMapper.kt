package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.tasks.mapper.queries.CardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.CardSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.CardUpdateQuery
import org.kevin.trello_v2.tasks.model.Card

@Mapper
interface CardMapper {
    fun insert(query: CardInsertQuery): Int
    fun update(query: CardUpdateQuery): Int
    fun findById(cardId: Long): Card?
    fun search(query: CardSearchQuery): List<Card>
}
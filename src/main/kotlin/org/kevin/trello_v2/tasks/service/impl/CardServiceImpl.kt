package org.kevin.trello_v2.tasks.service.impl

import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.mapper.CardMapper
import org.kevin.trello_v2.tasks.service.CardService
import org.kevin.trello_v2.tasks.service.vo.*
import org.kevin.trello_v2.tasks.constants.*
import org.kevin.trello_v2.tasks.mapper.queries.CardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.CardSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.CardUpdateQuery
import org.kevin.trello_v2.tasks.model.MembershipRole
import org.kevin.trello_v2.tasks.repo.PathResult
import org.kevin.trello_v2.tasks.repo.TaskPathHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CardServiceImpl(
    private val cardMapper: CardMapper,
    private val pathHelper: TaskPathHelper,
): CardService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private fun validateCardTitle(title: String) {
        if (title.isBlank()) {
            throw IllegalArgumentException("Card title cannot be blank")
        }
        if (title.length > MAX_CARD_TITLE_LENGTH) {
            throw IllegalArgumentException("Card title exceeds maximum length of $MAX_CARD_TITLE_LENGTH characters")
        }
    }

    private fun validateCardDescription(description: String) {
        if (description.isBlank()) {
            throw IllegalArgumentException("Card description cannot be blank")
        }
        if (description.length > MAX_CARD_DESCRIPTION_LENGTH) {
            throw IllegalArgumentException("Card description exceeds maximum length of $MAX_CARD_DESCRIPTION_LENGTH characters")
        }
    }

    private fun validatePermission(user: Account, cardId: Long): PathResult =
        pathHelper.pathOfCard(user.uid, cardId).also {
            it.boardView?.let { v ->
                if (v.closed) throw TrelloException("Closed board is read-only")
                if (v.role == MembershipRole.VIEWER)
                    throw TrelloException("Viewer cannot modify card")
            } ?: throw TrelloException("Card not exist")
        }


    @Transactional
    override fun createCard(vo: CreateCardVO): ApiResponse {
        val (title, listId, user) = vo

        // validate the parameters
        validateCardTitle(title)

        // validate permission and existence
        pathHelper.pathOfTaskList(user.uid, listId).boardView?.let {
            if (it.closed) throw TrelloException("Closed board is read-only")
            if (it.role == MembershipRole.VIEWER) throw TrelloException("Viewer cannot create card")
        } ?: throw TrelloException("List not exist")

        // calculate the position
        val position = CardSearchQuery(
            listId = listId,
        )
            .let { cardMapper.search(it) }
            .maxOfOrNull { it.position } ?: 0.0
            .plus(CARD_POSITION_INTERVAL)

        // insert into database
        val cardId = CardInsertQuery(
            title = title,
            position = position,
            listId = listId,
        ).let {
            val count = cardMapper.insert(it)
            if (count != 1) throw TrelloException("failed to create card")
            it.id
        }

        if (cardId == null) throw TrelloException("failed to create card")

        return ApiResponse.success()
            .message("Card created successfully")
            .add("cardId" to cardId)
            .add("position" to position)
            .build()
    }

    @Transactional
    override fun editCard(vo: CardEditVO): ApiResponse {
        // validate the parameters
        if (vo.isEmpty()) throw TrelloException("No changes happened")
        val (title, description, dueDate, cardId, user) = vo
        if (title != null) validateCardTitle(title)
        if (description != null) validateCardDescription(description)

        // validate permission and existence
        validatePermission(user, cardId)

        // update the card
        CardUpdateQuery(
            id = cardId,
            title = title,
            description = description,
            dueDate = dueDate,
        )
            .let {
                val count = cardMapper.update(it)
                if (count != 1) throw TrelloException("failed to edit card")
            }

        return ApiResponse.success()
            .message("Card edited successfully")
            .build()
    }

    @Transactional
    override fun moveCard(vo: CardMoveVO): ApiResponse {
        val (cardId, listId, afterId, user) = vo

        // validate the parameters
        // if afterId is not set, listId must be set (moving to head of a different list)
        if (afterId == null && listId == null)
            throw IllegalArgumentException("Either listId or afterId must be set")
        // afterId cannot be the card itself
        if (afterId == cardId)
            throw IllegalArgumentException("Card cannot be moved after itself")

        // validate permission and existence
        val (board, _, _, _) = validatePermission(user, cardId)

        // board cannot be null, and closed after validation
        assert(board != null && !board.closed) { "Board must exist and not be closed after invalidation" }

        var newPosition: Double
        var newList: Long

        if (afterId != null) {
            // it must exist
            val pathResult = pathHelper.pathOfCard(user.uid, afterId)
            if (pathResult.boardView == null)
                throw TrelloException("Card with AfterId not found")

            // it must be in the same board as the card
            if (pathResult.board != board)
                throw TrelloException("Cannot move card to a different board")

            // decide the new position
            val cards = CardSearchQuery(listId = pathResult.taskList!!.id)
                .let { cardMapper.search(it) }
                .sortedBy { it.position }

            val afterIndex = cards.indexOfFirst { it.id == afterId }
            val beforeIndex = afterIndex + 1

            val prevPosition = cards[afterIndex].position
            val nextPosition = if (beforeIndex < cards.size)
                cards[beforeIndex].position
            else
                prevPosition + CARD_POSITION_INTERVAL

            newPosition = (prevPosition + nextPosition) / 2
            newList = pathResult.taskList.id
        } else {
            // if afterId is unset, listId must be set
            assert(listId != null) { "listId must be set when afterId is not set" }

            // list must exist
            val pathResult = pathHelper.pathOfTaskList(user.uid, listId!!)

            // it must be in the same board as the card
            if (pathResult.board != board)
                throw TrelloException("Cannot move card to a different board")

            // decide the new position
            newPosition = CardSearchQuery(listId = listId)
                .let { cardMapper.search(it) }
                .minOfOrNull { it.position }
                ?.div(2) ?: CARD_POSITION_INTERVAL
            newList = listId
        }

        CardUpdateQuery(
            id = cardId,
            position = newPosition,
            listId = newList
        ).let {
            val count = cardMapper.update(it)
            if (count != 1) throw TrelloException("failed to move card")
        }

        return ApiResponse.success()
            .message("Card moved successfully")
            .add("newPosition" to newPosition)
            .add("newListId" to newList)
            .build()
    }

    @Transactional
    override fun completeTask(
        cardId: Long,
        user: Account
    ): ApiResponse {
        val card = validatePermission(user, cardId).card ?: throw TrelloException("Card not found")
        if (card.finished) throw TrelloException("Card is already completed")

        CardUpdateQuery(
            id = cardId,
            finished = true
        ).let {
            val count = cardMapper.update(it)
            if (count != 1) throw TrelloException("failed to complete card")
        }

        return ApiResponse.success()
            .message("Card completed successfully")
            .build()
    }

    @Transactional
    override fun incompleteTask(
        cardId: Long,
        user: Account
    ): ApiResponse {
        val card = validatePermission(user, cardId).card ?: throw TrelloException("Card not found")
        if (!card.finished) throw TrelloException("Card is still incomplete")

        CardUpdateQuery(
            id = cardId,
            finished = false
        ).let {
            val count = cardMapper.update(it)
            if (count != 1) throw TrelloException("failed to incomplete card")
        }

        return ApiResponse.success()
            .message("Card incompleted successfully")
            .build()
    }

    @Transactional
    override fun archiveTask(
        cardId: Long,
        user: Account
    ): ApiResponse {
        validatePermission(user, cardId)

        CardUpdateQuery(
            id = cardId,
            archived = true
        ).let {
            val count = cardMapper.update(it)
            if (count != 1) throw TrelloException("failed to incomplete card")
        }

        return ApiResponse.success()
            .message("Card archived successfully")
            .build()
    }
}
package org.kevin.trello_v2.tasks.repo.impl

import com.github.benmanes.caffeine.cache.Caffeine
import org.kevin.trello_v2.config.CacheProperties
import org.kevin.trello_v2.tasks.mapper.BoardMapper
import org.kevin.trello_v2.tasks.mapper.queries.BoardInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.BoardUpdateQuery
import org.kevin.trello_v2.tasks.model.Board
import org.kevin.trello_v2.tasks.repo.BoardRepo
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class BoardRepoImpl(
    private val boardMapper: BoardMapper,
    cacheProperties: CacheProperties,
): BoardRepo {
    private val cache = Caffeine
        .newBuilder()
        .maximumSize(cacheProperties.boardMaxSize)
        .expireAfterAccess(cacheProperties.boardLifeMinutes, TimeUnit.MINUTES)
        .build<String, Board>()

    override fun search(query: BoardSearchQuery): List<Board> =
        boardMapper.search(query).let {
            it.forEach { cache.put(it.id, it) }
            it
        }

    override fun findById(id: String): Board? =
        cache.getIfPresent(id)
            ?: boardMapper.findById(id)?.also { cache.put(id, it) }

    override fun insert(query: BoardInsertQuery): Int =
        boardMapper.insert(query)

    override fun updateById(query: BoardUpdateQuery): Int =
        boardMapper.updateById(query).also {
            if (it == 1) cache.invalidate(query.id)
        }
}
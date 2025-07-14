package org.kevin.trello_v2.tasks.repo.impl

import com.github.benmanes.caffeine.cache.Caffeine
import org.kevin.trello_v2.config.CacheProperties
import org.kevin.trello_v2.tasks.mapper.BoardMemberMapper
import org.kevin.trello_v2.tasks.mapper.queries.MembershipInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.MembershipUpdateQuery
import org.kevin.trello_v2.tasks.model.BoardMembership
import org.kevin.trello_v2.tasks.repo.MemberRepo
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class MemberRepoImpl(
    private val memberMapper: BoardMemberMapper,
    cacheProperties: CacheProperties,
): MemberRepo {
    private val cache = Caffeine
        .newBuilder()
        .maximumSize(cacheProperties.memberMaxSize)
        .expireAfterAccess(cacheProperties.memberLifeMinutes, TimeUnit.MINUTES)
        .build<Pair<String, String>, BoardMembership>()

    override fun findByKey(
        userUid: String,
        boardId: String
    ): BoardMembership? =
        cache.getIfPresent(userUid to boardId)
            ?: memberMapper.findByKey(userUid, boardId)?.also { cache.put(userUid to boardId, it) }

    override fun search(query: MembershipSearchQuery): List<BoardMembership> =
        memberMapper.search(query)
            .also { it.forEach { cache.put(it.userUid to it.boardId, it) } }

    override fun insert(query: MembershipInsertQuery): Int =
        memberMapper.insert(query)

    override fun update(query: MembershipUpdateQuery): Int =
        memberMapper.update(query).also {
            if (it == 1) cache.invalidate(query.userUid to query.boardId)
        }
}
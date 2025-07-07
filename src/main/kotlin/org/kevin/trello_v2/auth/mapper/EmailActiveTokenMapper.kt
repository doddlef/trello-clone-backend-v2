package org.kevin.trello_v2.auth.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.auth.model.EmailActiveToken

@Mapper
interface EmailActiveTokenMapper {
    fun insertToken(token: EmailActiveToken): Int
    fun findByContent(content: String): EmailActiveToken?
    fun findByUid(uid: String): EmailActiveToken?
    fun deleteByContent(content: String): Int
    fun deleteAllExpiredTokens(): Int

    // Testing only
    fun findByEmail(email: String): EmailActiveToken?
}
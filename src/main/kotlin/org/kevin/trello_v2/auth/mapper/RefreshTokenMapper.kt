package org.kevin.trello_v2.auth.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.auth.model.RefreshToken

@Mapper
interface RefreshTokenMapper {
    fun save(token: RefreshToken): Int
    fun findByContent(content: String): RefreshToken?
    fun deleteByContent(content: String): Int
    fun deleteAllExpired(): Int
}
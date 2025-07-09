package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello_v2.framework.PgEnumTypeHandler
import org.kevin.trello_v2.tasks.model.Board
import org.kevin.trello_v2.tasks.model.BoardVisibility
import org.kevin.trello_v2.tasks.model.MembershipRole

@MappedTypes(Board::class)
@MappedJdbcTypes(JdbcType.OTHER)
class BoardVisibilityTypeHandler: PgEnumTypeHandler<BoardVisibility>(BoardVisibility::class.java)

@MappedTypes(MembershipRole::class)
@MappedJdbcTypes(JdbcType.OTHER)
class MembershipRoleTypeHandler: PgEnumTypeHandler<MembershipRole>(MembershipRole::class.java)
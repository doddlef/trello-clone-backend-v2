package org.kevin.trello_v2.tasks.mapper.typeHandlers

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello_v2.framework.PgEnumTypeHandler
import org.kevin.trello_v2.tasks.model.MembershipRole

@MappedTypes(MembershipRole::class)
@MappedJdbcTypes(JdbcType.OTHER)
class MembershipRoleTypeHandler: PgEnumTypeHandler<MembershipRole>(MembershipRole::class.java)
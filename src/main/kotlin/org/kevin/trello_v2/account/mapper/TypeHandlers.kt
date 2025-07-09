package org.kevin.trello_v2.account.mapper

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.kevin.trello_v2.account.model.AccountRole
import org.kevin.trello_v2.account.model.AccountStatus
import org.kevin.trello_v2.framework.PgEnumTypeHandler

@MappedTypes(AccountRole::class)
@MappedJdbcTypes(JdbcType.OTHER)
class AccountRoleTypeHandler: PgEnumTypeHandler<AccountRole>(AccountRole::class.java)

@MappedTypes(AccountStatus::class)
@MappedJdbcTypes(JdbcType.OTHER)
class AccountStatusTypeHandler: PgEnumTypeHandler<AccountStatus>(AccountStatus::class.java)
package org.kevin.trello_v2.framework

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

open class PgEnumTypeHandler<T : Enum<T>>(
    private val enumType: Class<T>
) : BaseTypeHandler<T>() {

    @Throws(SQLException::class)
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType?) {
        ps.setObject(i, parameter.name, Types.OTHER)
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnName: String): T? {
        return toEnum(rs.getString(columnName))
    }

    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet, columnIndex: Int): T? {
        return toEnum(rs.getString(columnIndex))
    }

    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): T? {
        throw UnsupportedOperationException()
    }

    open fun toEnum(name: String?): T? {
        return if (name == null) null else java.lang.Enum.valueOf(enumType, name)
    }
}
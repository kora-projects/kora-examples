package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import java.sql.PreparedStatement
import java.sql.Types

@Component
class ListOfLongJdbcParameterMapper : JdbcParameterColumnMapper<List<Long>> {

    // the 2.0 contract declares the value as @Nullable, which Kotlin enforces on the override
    override fun set(stmt: PreparedStatement, index: Int, value: List<Long>?) {
        if (value == null) {
            stmt.setNull(index, Types.ARRAY)
            return
        }

        val sqlArray = stmt.connection.createArrayOf("BIGINT", value.toTypedArray())
        stmt.setArray(index, sqlArray)
    }
}

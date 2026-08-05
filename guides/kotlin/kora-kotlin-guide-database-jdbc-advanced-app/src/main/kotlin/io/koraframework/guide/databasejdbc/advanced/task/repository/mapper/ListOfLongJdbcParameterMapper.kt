package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import java.sql.PreparedStatement

@Component
class ListOfLongJdbcParameterMapper : JdbcParameterColumnMapper<List<Long>> {

    override fun set(stmt: PreparedStatement, index: Int, value: List<Long>) {
        val sqlArray = stmt.connection.createArrayOf("BIGINT", value.toTypedArray())
        stmt.setArray(index, sqlArray)
    }
}

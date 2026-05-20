package ru.tinkoff.kora.kotlin.example.crud.repository

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.kotlin.example.crud.model.Pet
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

@Component
class PetStatusParameterMapper : JdbcParameterColumnMapper<Pet.Status> {

    @Throws(SQLException::class)
    override fun set(stmt: PreparedStatement, index: Int, value: Pet.Status?) {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER)
        } else {
            stmt.setInt(index, value.code)
        }
    }
}

@Component
class PetStatusResultMapper : JdbcResultColumnMapper<Pet.Status> {

    private val statuses: Array<Pet.Status> = Pet.Status.entries.toTypedArray()

    @Throws(SQLException::class)
    override fun apply(row: ResultSet, index: Int): Pet.Status {
        val code = row.getInt(index)
        for (status in statuses) {
            if (code == status.code) {
                return status
            }
        }

        throw IllegalStateException("Unknown code: $code")
    }
}

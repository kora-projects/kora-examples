package ru.tinkoff.kora.kotlin.example.submodule.pet.repository

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

@Component
class PetStatusParameterMapper : JdbcParameterColumnMapper<Pet.Status> {
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
    override fun apply(row: ResultSet, index: Int): Pet.Status {
        val code = row.getInt(index)
        return Pet.Status.entries.firstOrNull { it.code == code }
            ?: throw IllegalStateException("Unknown code: $code")
    }
}

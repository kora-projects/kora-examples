package ru.tinkoff.kora.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper

@Repository
interface R2dbcMapperParameterRepository : R2dbcRepository {

    data class Entity(
        val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        @field:Nullable val value3: String?
    ) {
        enum class FieldType(val code: Int) {
            UNKNOWN(-10),
            ONE(1),
            TWO(2)
        }
    }

    @Query("UPDATE entities SET value1 = :fieldType WHERE id = :id")
    suspend fun updateFieldType(
        id: String,
        @Mapping(R2dbcEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType
    ): UpdateCount
}

class R2dbcEntityFieldTypeParameterMapper :
    R2dbcParameterColumnMapper<R2dbcMapperParameterRepository.Entity.FieldType> {
    override fun apply(stmt: Statement, index: Int, value: R2dbcMapperParameterRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.bindNull(index, Integer::class.java)
        } else {
            stmt.bind(index, value.code)
        }
    }
}


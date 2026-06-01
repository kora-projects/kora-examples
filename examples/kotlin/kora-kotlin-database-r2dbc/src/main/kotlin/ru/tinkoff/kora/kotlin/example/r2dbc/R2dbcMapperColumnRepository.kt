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
interface R2dbcMapperColumnRepository : R2dbcRepository {

    data class Entity(
        val id: String,
        @Mapping(R2dbcEntityFieldTypeResultMapper::class)
        @Mapping(R2dbcEntityFieldTypeColumnMapper::class)
        @field:Column("value1")
        val field1: FieldType,
        val value2: String,
        @field:Nullable val value3: String?
    ) {
        enum class FieldType(val code: Int) {
            UNKNOWN(-10),
            ONE(1),
            TWO(2)
        }
    }

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<Entity>
}

class R2dbcEntityFieldTypeResultMapper : R2dbcResultColumnMapper<R2dbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(row: Row, label: String): R2dbcMapperColumnRepository.Entity.FieldType? {
        val fieldAsInt = row.get(label, Integer::class.java)?.toInt() ?: return null
        return R2dbcMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: R2dbcMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}

class R2dbcEntityFieldTypeColumnMapper : R2dbcParameterColumnMapper<R2dbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(stmt: Statement, index: Int, value: R2dbcMapperColumnRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.bindNull(index, Integer::class.java)
        } else {
            stmt.bind(index, value.code)
        }
    }
}


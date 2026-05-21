package ru.tinkoff.kora.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.vertx.VertxRepository
import ru.tinkoff.kora.database.vertx.mapper.parameter.VertxParameterColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxResultColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowSetMapper

@Repository
interface VertxMapperColumnRepository : VertxRepository {
    data class Entity(
        val id: String,
        @Mapping(VertxEntityFieldTypeResultMapper::class)
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
    fun findAll(): List<Entity>
}

class VertxEntityFieldTypeResultMapper : VertxResultColumnMapper<VertxMapperColumnRepository.Entity.FieldType> {
    override fun apply(row: Row, index: Int): VertxMapperColumnRepository.Entity.FieldType? {
        val fieldAsInt = row.get(Integer::class.java, index)?.toInt() ?: return null
        return VertxMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: VertxMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}


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
interface VertxMapperParameterRepository : VertxRepository {

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
    fun updateFieldType(
        id: String,
        @Mapping(VertxEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType
    ): UpdateCount
}

class VertxEntityFieldTypeParameterMapper :
    VertxParameterColumnMapper<VertxMapperParameterRepository.Entity.FieldType?> {
    override fun apply(fieldType: VertxMapperParameterRepository.Entity.FieldType?): Any? {
        return fieldType?.code
    }
}


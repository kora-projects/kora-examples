package io.koraframework.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Batch
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.vertx.VertxRepository
import io.koraframework.database.vertx.mapper.parameter.VertxParameterColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxResultColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxRowMapper
import io.koraframework.database.vertx.mapper.result.VertxRowSetMapper

@Repository
interface VertxMapperColumnRepository : VertxRepository {

    data class Entity(
        val id: String,
        @Mapping(VertxEntityFieldTypeResultMapper::class)
        @field:Column("value1")
        val field1: FieldType,
        val value2: String,
        val value3: String?
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


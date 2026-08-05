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
interface VertxMapperRowRepository : VertxRepository {
    @Mapping(VertxEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<VertxEntityPart>
}

data class VertxEntityPart(val id: String, val field1: Int)

class VertxEntityPartRowMapper : VertxRowMapper<VertxEntityPart> {
    override fun apply(row: Row): VertxEntityPart {
        return VertxEntityPart(row.get(String::class.java, 0), row.get(Integer::class.java, 1).toInt())
    }
}


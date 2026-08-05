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
interface VertxMapperRowSetRepository : VertxRepository {
    @Mapping(VertxEntityPartRowSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<VertxEntityPart>>
}

class VertxEntityPartRowSetMapper : VertxRowSetMapper<Map<Int, List<VertxEntityPart>>> {
    override fun apply(rows: RowSet<Row>): Map<Int, List<VertxEntityPart>> {
        val result = LinkedHashMap<Int, MutableList<VertxEntityPart>>(rows.size())
        for (row in rows) {
            val entityPart = VertxEntityPart(row.getString(0), row.getInteger(1))
            result.computeIfAbsent(entityPart.field1) { ArrayList() }.add(entityPart)
        }
        return result
    }
}


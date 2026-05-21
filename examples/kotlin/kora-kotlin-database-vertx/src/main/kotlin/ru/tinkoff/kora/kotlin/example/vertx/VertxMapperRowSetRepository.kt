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


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
interface R2dbcMapperRowRepository : R2dbcRepository {

    @Mapping(R2dbcEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    suspend fun findAllParts(): List<R2dbcEntityPart>
}

data class R2dbcEntityPart(val id: String, @field:Column("value1") val field1: Int)

class R2dbcEntityPartRowMapper : R2dbcRowMapper<R2dbcEntityPart> {
    override fun apply(row: Row): R2dbcEntityPart {
        return R2dbcEntityPart(row.get(0, String::class.java)!!, row.get(1, Integer::class.java)!!.toInt())
    }
}


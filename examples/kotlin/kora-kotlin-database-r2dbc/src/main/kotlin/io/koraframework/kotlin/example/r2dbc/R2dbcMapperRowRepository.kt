package io.koraframework.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.koraframework.common.annotation.Mapping
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.r2dbc.R2dbcRepository
import io.koraframework.database.r2dbc.mapper.result.R2dbcRowMapper

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


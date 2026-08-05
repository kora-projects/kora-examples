package io.koraframework.kotlin.example.r2dbc

import io.r2dbc.spi.Statement
import io.koraframework.common.annotation.Mapping
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.r2dbc.R2dbcRepository
import io.koraframework.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper

@Repository
interface R2dbcMapperParameterRepository : R2dbcRepository {

    data class Entity(
        val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        val value3: String?
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


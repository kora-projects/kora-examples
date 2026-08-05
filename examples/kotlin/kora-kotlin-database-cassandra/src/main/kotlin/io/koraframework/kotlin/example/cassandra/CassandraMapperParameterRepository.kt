package io.koraframework.kotlin.example.cassandra

import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.data.GettableByName
import com.datastax.oss.driver.api.core.data.SettableByName
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.database.cassandra.CassandraRepository
import io.koraframework.database.cassandra.annotation.EntityCassandra
import io.koraframework.database.cassandra.annotation.UDT
import io.koraframework.database.cassandra.mapper.parameter.CassandraParameterColumnMapper
import io.koraframework.database.cassandra.mapper.result.CassandraResultSetMapper
import io.koraframework.database.cassandra.mapper.result.CassandraRowColumnMapper
import io.koraframework.database.cassandra.mapper.result.CassandraRowMapper
import io.koraframework.database.common.annotation.Batch
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository

@Repository
interface CassandraMapperParameterRepository : CassandraRepository {

    @EntityCassandra
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
    fun updateFieldType(
        id: String,
        @Mapping(CassandraEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType
    )
}

class CassandraEntityFieldTypeParameterMapper :
    CassandraParameterColumnMapper<CassandraMapperParameterRepository.Entity.FieldType> {
    override fun apply(
        stmt: SettableByName<*>,
        index: Int,
        value: CassandraMapperParameterRepository.Entity.FieldType?
    ) {
        if (value != null) {
            stmt.setInt(index, value.code)
        }
    }
}


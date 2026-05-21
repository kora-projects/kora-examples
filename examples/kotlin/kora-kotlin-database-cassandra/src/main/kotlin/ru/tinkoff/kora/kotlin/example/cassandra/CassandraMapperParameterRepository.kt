package ru.tinkoff.kora.kotlin.example.cassandra

import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.data.GettableByName
import com.datastax.oss.driver.api.core.data.SettableByName
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.cassandra.CassandraRepository
import ru.tinkoff.kora.database.cassandra.annotation.EntityCassandra
import ru.tinkoff.kora.database.cassandra.annotation.UDT
import ru.tinkoff.kora.database.cassandra.mapper.parameter.CassandraParameterColumnMapper
import ru.tinkoff.kora.database.cassandra.mapper.result.CassandraResultSetMapper
import ru.tinkoff.kora.database.cassandra.mapper.result.CassandraRowColumnMapper
import ru.tinkoff.kora.database.cassandra.mapper.result.CassandraRowMapper
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository

@Repository
interface CassandraMapperParameterRepository : CassandraRepository {
    @EntityCassandra
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


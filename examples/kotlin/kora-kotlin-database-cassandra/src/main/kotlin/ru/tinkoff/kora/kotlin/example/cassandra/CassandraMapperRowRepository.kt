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
interface CassandraMapperRowRepository : CassandraRepository {
    @Mapping(CassandraEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<CassandraEntityPart>
}

@EntityCassandra
data class CassandraEntityPart(val id: String, @field:Column("value1") val field1: Int)

class CassandraEntityPartRowMapper : CassandraRowMapper<CassandraEntityPart> {
    override fun apply(row: Row): CassandraEntityPart {
        return CassandraEntityPart(row.getString(0)!!, row.getInt(1))
    }
}


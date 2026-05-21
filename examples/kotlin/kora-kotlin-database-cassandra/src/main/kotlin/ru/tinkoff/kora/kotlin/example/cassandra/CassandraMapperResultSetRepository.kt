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
interface CassandraMapperResultSetRepository : CassandraRepository {
    @Mapping(CassandraEntityPartResultSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<CassandraEntityPart>>
}

class CassandraEntityPartResultSetMapper : CassandraResultSetMapper<Map<Int, List<CassandraEntityPart>>> {
    override fun apply(rows: ResultSet): Map<Int, List<CassandraEntityPart>> {
        val result = LinkedHashMap<Int, MutableList<CassandraEntityPart>>()
        for (row in rows.all()) {
            val entityPart = CassandraEntityPart(row.getString(0)!!, row.getInt(1))
            result.computeIfAbsent(entityPart.field1) { ArrayList() }.add(entityPart)
        }
        return result
    }
}


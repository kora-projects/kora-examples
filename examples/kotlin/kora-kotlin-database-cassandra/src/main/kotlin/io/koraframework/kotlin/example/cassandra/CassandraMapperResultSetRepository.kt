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


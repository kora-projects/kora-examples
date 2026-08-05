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
interface CassandraCrudSuspendRepository : CassandraRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): CassandraEntity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<CassandraEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: CassandraEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: CassandraEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("TRUNCATE entities")
    suspend fun deleteAll()
}


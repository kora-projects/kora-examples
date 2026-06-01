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


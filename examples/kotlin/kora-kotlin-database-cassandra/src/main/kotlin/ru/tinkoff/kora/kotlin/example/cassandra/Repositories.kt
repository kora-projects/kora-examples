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

@EntityCassandra
data class CassandraEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface CassandraCrudSyncRepository : CassandraRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): CassandraEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<CassandraEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: CassandraEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<CassandraEntity>)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: CassandraEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun updateBatch(@Batch entity: List<CassandraEntity>)

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String)

    @Query("TRUNCATE entities")
    fun deleteAll()
}

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

@EntityCassandra
data class CassandraUdtEntity(val id: String, val name: Name) {
    @UDT
    data class Name(val first: String, val last: String)
}

@Repository
interface CassandraUdtRepository : CassandraRepository {
    @Query("SELECT * FROM entities_udt WHERE id = :id")
    fun findById(id: String): CassandraUdtEntity?

    @Query("INSERT INTO entities_udt(id, name) VALUES (:entity.id, :entity.name)")
    fun insert(entity: CassandraUdtEntity)
}

class CassandraEntityFieldTypeResultMapper :
    CassandraRowColumnMapper<CassandraMapperRowColumnRepository.Entity.FieldType> {
    override fun apply(row: GettableByName, index: Int): CassandraMapperRowColumnRepository.Entity.FieldType {
        val fieldAsInt = row.getInt(index)
        return CassandraMapperRowColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: CassandraMapperRowColumnRepository.Entity.FieldType.UNKNOWN
    }
}

class CassandraEntityFieldTypeColumnMapper :
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

@Repository
interface CassandraMapperRowColumnRepository : CassandraRepository {
    @EntityCassandra
    data class Entity(
        val id: String,
        @Mapping(CassandraEntityFieldTypeResultMapper::class)
        @Mapping(CassandraEntityFieldTypeColumnMapper::class)
        @field:Column("value1")
        val field1: FieldType,
        val value2: String,
        @field:Nullable val value3: String?
    ) {
        enum class FieldType(val code: Int) {
            UNKNOWN(-10),
            ONE(1),
            TWO(2)
        }
    }

    @Query("SELECT * FROM entities")
    fun findAll(): List<Entity>
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

@EntityCassandra
data class CassandraEntityPart(val id: String, @field:Column("value1") val field1: Int)

class CassandraEntityPartRowMapper : CassandraRowMapper<CassandraEntityPart> {
    override fun apply(row: Row): CassandraEntityPart {
        return CassandraEntityPart(row.getString(0)!!, row.getInt(1))
    }
}

@Repository
interface CassandraMapperRowRepository : CassandraRepository {
    @Mapping(CassandraEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<CassandraEntityPart>
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

@Repository
interface CassandraMapperResultSetRepository : CassandraRepository {
    @Mapping(CassandraEntityPartResultSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<CassandraEntityPart>>
}

@Root
@Component
class RootService(
    private val crudSyncRepository: CassandraCrudSyncRepository,
    private val crudSuspendRepository: CassandraCrudSuspendRepository,
    private val cassandraUdtRepository: CassandraUdtRepository,
    private val mapperRowColumnRepository: CassandraMapperRowColumnRepository,
    private val mapperParameterRepository: CassandraMapperParameterRepository,
    private val mapperRowRepository: CassandraMapperRowRepository,
    private val mapperResultSetRepository: CassandraMapperResultSetRepository
)

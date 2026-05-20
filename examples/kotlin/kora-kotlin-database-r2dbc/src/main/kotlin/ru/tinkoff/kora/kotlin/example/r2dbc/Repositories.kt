package ru.tinkoff.kora.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper
import java.util.*

data class R2dbcEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface R2dbcCrudSuspendRepository : R2dbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): R2dbcEntity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<R2dbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: R2dbcEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: R2dbcEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

@Repository
interface R2dbcCrudSyncRepository : R2dbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): R2dbcEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<R2dbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: R2dbcEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<R2dbcEntity>): UpdateCount

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: R2dbcEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun updateBatch(@Batch entity: List<R2dbcEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Table("entities")
data class R2dbcMacrosEntity(
    @field:Id val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface R2dbcCrudMacrosRepository : R2dbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    suspend fun findById(id: String): R2dbcMacrosEntity?

    @Query("SELECT %{return#selects} FROM %{return#table}")
    suspend fun findAll(): List<R2dbcMacrosEntity>

    @Query("INSERT INTO %{entity#inserts}")
    suspend fun insert(entity: R2dbcMacrosEntity)

    @Query("UPDATE %{entity#updates} WHERE %{entity#where = @id}")
    suspend fun update(entity: R2dbcMacrosEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

@Repository
interface R2dbcIdRandomRepository : R2dbcRepository {
    data class Entity(val id: UUID = UUID.randomUUID(), @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    suspend fun findById(id: UUID): Entity?

    @Query("INSERT INTO entities_uuid(id, name) VALUES (:entity.id, :entity.name)")
    suspend fun insert(entity: Entity): UpdateCount
}

@Repository
interface R2dbcIdSequenceRepository : R2dbcRepository {
    data class Entity(val id: Long?, @field:Column("name") val name: String) {
        constructor(name: String) : this(null, name)
    }

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    suspend fun findById(id: Long): Entity?

    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name) RETURNING id")
    suspend fun insert(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    suspend fun createGenerated(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    suspend fun createGenerated(@Batch entity: List<Entity>): List<Long>
}

class R2dbcEntityFieldTypeResultMapper : R2dbcResultColumnMapper<R2dbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(row: Row, label: String): R2dbcMapperColumnRepository.Entity.FieldType? {
        val fieldAsInt = row.get(label, Integer::class.java)?.toInt() ?: return null
        return R2dbcMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: R2dbcMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}

class R2dbcEntityFieldTypeColumnMapper : R2dbcParameterColumnMapper<R2dbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(stmt: Statement, index: Int, value: R2dbcMapperColumnRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.bindNull(index, Integer::class.java)
        } else {
            stmt.bind(index, value.code)
        }
    }
}

@Repository
interface R2dbcMapperColumnRepository : R2dbcRepository {
    data class Entity(
        val id: String,
        @Mapping(R2dbcEntityFieldTypeResultMapper::class)
        @Mapping(R2dbcEntityFieldTypeColumnMapper::class)
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
    suspend fun findAll(): List<Entity>
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

@Repository
interface R2dbcMapperParameterRepository : R2dbcRepository {
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
    suspend fun updateFieldType(
        id: String,
        @Mapping(R2dbcEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType
    ): UpdateCount
}

data class R2dbcEntityPart(val id: String, @field:Column("value1") val field1: Int)

class R2dbcEntityPartRowMapper : R2dbcRowMapper<R2dbcEntityPart> {
    override fun apply(row: Row): R2dbcEntityPart {
        return R2dbcEntityPart(row.get(0, String::class.java)!!, row.get(1, Integer::class.java)!!.toInt())
    }
}

@Repository
interface R2dbcMapperRowRepository : R2dbcRepository {
    @Query("SELECT id, value1 FROM entities")
    suspend fun findAllParts(): List<R2dbcEntityPart>
}

@Root
@Component
class RootService(
    private val r2dbcCrudSyncRepository: R2dbcCrudSyncRepository
)

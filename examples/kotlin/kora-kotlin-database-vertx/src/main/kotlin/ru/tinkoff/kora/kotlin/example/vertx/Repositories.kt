package ru.tinkoff.kora.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.vertx.VertxRepository
import ru.tinkoff.kora.database.vertx.mapper.parameter.VertxParameterColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxResultColumnMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowMapper
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowSetMapper

data class VertxEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface VertxCrudSyncRepository : VertxRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): VertxEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<VertxEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: VertxEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<VertxEntity>): UpdateCount

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: VertxEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun updateBatch(@Batch entity: List<VertxEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Repository
interface VertxCrudSuspendRepository : VertxRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): VertxEntity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<VertxEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: VertxEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: VertxEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

class VertxEntityFieldTypeResultMapper : VertxResultColumnMapper<VertxMapperColumnRepository.Entity.FieldType> {
    override fun apply(row: Row, index: Int): VertxMapperColumnRepository.Entity.FieldType? {
        val fieldAsInt = row.get(Integer::class.java, index)?.toInt() ?: return null
        return VertxMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: VertxMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}

@Repository
interface VertxMapperColumnRepository : VertxRepository {
    data class Entity(
        val id: String,
        @Mapping(VertxEntityFieldTypeResultMapper::class)
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

class VertxEntityFieldTypeParameterMapper :
    VertxParameterColumnMapper<VertxMapperParameterRepository.Entity.FieldType?> {
    override fun apply(fieldType: VertxMapperParameterRepository.Entity.FieldType?): Any? {
        return fieldType?.code
    }
}

@Repository
interface VertxMapperParameterRepository : VertxRepository {
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
        @Mapping(VertxEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType
    ): UpdateCount
}

data class VertxEntityPart(val id: String, val field1: Int)

class VertxEntityPartRowMapper : VertxRowMapper<VertxEntityPart> {
    override fun apply(row: Row): VertxEntityPart {
        return VertxEntityPart(row.get(String::class.java, 0), row.get(Integer::class.java, 1).toInt())
    }
}

@Repository
interface VertxMapperRowRepository : VertxRepository {
    @Mapping(VertxEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<VertxEntityPart>
}

class VertxEntityPartRowSetMapper : VertxRowSetMapper<Map<Int, List<VertxEntityPart>>> {
    override fun apply(rows: RowSet<Row>): Map<Int, List<VertxEntityPart>> {
        val result = LinkedHashMap<Int, MutableList<VertxEntityPart>>(rows.size())
        for (row in rows) {
            val entityPart = VertxEntityPart(row.getString(0), row.getInteger(1))
            result.computeIfAbsent(entityPart.field1) { ArrayList() }.add(entityPart)
        }
        return result
    }
}

@Repository
interface VertxMapperRowSetRepository : VertxRepository {
    @Mapping(VertxEntityPartRowSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<VertxEntityPart>>
}

@Root
@Component
class RootService(
    private val vertxCrudRepository: VertxCrudSyncRepository,
    private val vertxCrudSuspendRepository: VertxCrudSuspendRepository,
    private val vertxMapperColumnRepository: VertxMapperColumnRepository,
    private val vertxMapperParameterRepository: VertxMapperParameterRepository,
    private val vertxMapperRowRepository: VertxMapperRowRepository,
    private val vertxMapperRowSetRepository: VertxMapperRowSetRepository
)

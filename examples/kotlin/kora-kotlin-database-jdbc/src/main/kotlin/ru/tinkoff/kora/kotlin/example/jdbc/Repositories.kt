package ru.tinkoff.kora.kotlin.example.jdbc

import jakarta.annotation.Nullable
import org.postgresql.util.PGobject
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultSetMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcRowMapper
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.*

@EntityJdbc
data class JdbcEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Component
class ListOfStringJdbcParameterMapper : JdbcParameterColumnMapper<List<String>> {
    override fun set(stmt: PreparedStatement, index: Int, value: List<String>) {
        stmt.setArray(index, stmt.connection.createArrayOf("VARCHAR", value.toTypedArray()))
    }
}

@Repository
interface JdbcCrudSyncRepository : JdbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): JdbcEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<JdbcEntity>

    @Query("SELECT * FROM entities WHERE id = ANY(:ids)")
    fun findAllByIds(@Mapping(ListOfStringJdbcParameterMapper::class) ids: List<String>): List<JdbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: JdbcEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<JdbcEntity>): UpdateCount

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: JdbcEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun updateBatch(@Batch entity: List<JdbcEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Table("entities")
data class JdbcMacrosEntity(
    @field:Id val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface JdbcCrudMacrosRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: String): JdbcMacrosEntity?

    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<JdbcMacrosEntity>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: JdbcMacrosEntity): UpdateCount

    @Query("INSERT INTO %{entity#inserts}")
    fun insertBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: JdbcMacrosEntity): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun updateBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    fun upsert(entity: JdbcMacrosEntity): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    fun upsertBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String): UpdateCount

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Repository
interface JdbcIdRandomRepository : JdbcRepository {
    data class Entity(val id: UUID = UUID.randomUUID(), @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    fun findById(id: UUID): Entity?

    @Query("INSERT INTO entities_uuid(id, name) VALUES (:entity.id, :entity.name)")
    fun insert(entity: Entity): UpdateCount
}

@Repository
interface JdbcIdSequenceRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(@field:Id val id: Long?, @field:Column("name") val name: String) {
        constructor(name: String) : this(null, name)
    }

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    fun findById(id: Long): Entity?

    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name) RETURNING id")
    fun insert(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    fun insertGenerated(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    fun insertGenerated(@Batch entity: List<Entity>): List<Long>
}

@Repository
interface JdbcIdRandomCompositeRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(
        @field:Id @field:Embedded val id: EntityId = EntityId(),
        @field:Column("name") val name: String
    ) {
        constructor(name: String) : this(EntityId(), name)

        data class EntityId(val a: UUID = UUID.randomUUID(), val b: UUID = UUID.randomUUID())
    }

    @Query("SELECT * FROM entities_composite_uuid WHERE a = :id.a AND b = :id.b")
    fun findById(id: Entity.EntityId): Entity?

    @Query("INSERT INTO entities_composite_uuid(a, b, name) VALUES (:entity.id.a, :entity.id.b, :entity.name)")
    fun insert(entity: Entity): UpdateCount

    @Query("INSERT INTO entities_composite_uuid(a, b, name) VALUES (:entity.id.a, :entity.id.b, :entity.name)")
    fun insert(@Batch entity: List<Entity>): UpdateCount
}

@Repository
interface JdbcIdSequenceCompositeRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(
        @field:Id @field:Embedded val id: EntityId?,
        @field:Column("name") val name: String
    ) {
        constructor(name: String) : this(null, name)

        @EntityJdbc
        data class EntityId(val a: Long?, val b: Long?)
    }

    @Query("SELECT * FROM entities_composite WHERE a = :id.a AND b = :id.b")
    fun findById(id: Entity.EntityId): Entity?

    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insert(entity: Entity): UpdateCount

    @Id
    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insertGenerated(entity: Entity): Entity.EntityId

    @Id
    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insertGenerated(@Batch entity: List<Entity>): List<Entity.EntityId>
}

interface AbstractJdbcCrudRepository<K, V> : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<V>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: V): UpdateCount

    @Query("INSERT INTO %{entity#inserts}")
    fun insertBatch(@Batch entity: List<V>): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: V): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun updateBatch(@Batch entity: List<V>): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    fun upsert(entity: V): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    fun upsertBatch(@Batch entity: List<V>): UpdateCount

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    fun delete(entity: V): UpdateCount

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    fun deleteBatch(@Batch entity: List<V>): UpdateCount
}

@Repository
interface JdbcCrudExtendedRepository : AbstractJdbcCrudRepository<String, JdbcCrudExtendedRepository.Entity> {
    @EntityJdbc
    @Table("entities")
    data class Entity(
        @field:Id val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        @field:Nullable val value3: String?
    )

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: String): Optional<Entity>

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String): UpdateCount

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Repository
interface JdbcCrudExtendedCompositeRepository :
    AbstractJdbcCrudRepository<JdbcCrudExtendedCompositeRepository.Entity.EntityId, JdbcCrudExtendedCompositeRepository.Entity> {
    @EntityJdbc
    @Table("entities_composite_uuid")
    data class Entity(
        @field:Id @field:Embedded val id: EntityId,
        @field:Column("name") val name: String
    ) {
        data class EntityId(val a: UUID = UUID.randomUUID(), val b: UUID = UUID.randomUUID())
    }

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE %{id#where}")
    fun findById(id: Entity.EntityId): Optional<Entity>

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    fun deleteById(id: Entity.EntityId): UpdateCount

    @Query("DELETE FROM entities_composite_uuid")
    fun deleteAll(): UpdateCount
}

@Repository
interface JdbcCrudMacrosIdCompositeRepository : JdbcRepository {
    @EntityJdbc
    @Table("entities_composite_uuid")
    data class Entity(
        @field:Id @field:Embedded val id: EntityId,
        @field:Column("name") val name: String
    ) {
        data class EntityId(val a: UUID = UUID.randomUUID(), val b: UUID = UUID.randomUUID())
    }

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE %{id#where}")
    fun findById(id: Entity.EntityId): Optional<Entity>

    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<Entity>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: Entity): UpdateCount

    @Query("INSERT INTO %{entity#inserts}")
    fun insertBatch(@Batch entity: List<Entity>): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Entity): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun updateBatch(@Batch entity: List<Entity>): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (a, b) DO UPDATE SET %{entity#updates}")
    fun upsert(entity: Entity): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (a, b) DO UPDATE SET %{entity#updates}")
    fun upsertBatch(@Batch entity: List<Entity>): UpdateCount

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    fun deleteById(id: Entity.EntityId): UpdateCount

    @Query("DELETE FROM entities_composite_uuid")
    fun deleteAll(): UpdateCount
}

@Repository
interface JdbcCrudSuspendRepository : JdbcRepository {
    @EntityJdbc
    @Table("entities")
    data class Entity(
        @field:Id val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        @field:Nullable val value3: String?
    )

    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): Entity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<Entity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: Entity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: Entity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

class JdbcEntityFieldTypeResultMapper : JdbcResultColumnMapper<JdbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(rs: ResultSet, index: Int): JdbcMapperColumnRepository.Entity.FieldType {
        val fieldAsInt = rs.getInt(index)
        return JdbcMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: JdbcMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}

class JdbcEntityFieldTypeColumnMapper : JdbcParameterColumnMapper<JdbcMapperColumnRepository.Entity.FieldType> {
    override fun set(stmt: PreparedStatement, index: Int, value: JdbcMapperColumnRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER)
        } else {
            stmt.setInt(index, value.code)
        }
    }
}

@Repository
interface JdbcMapperColumnRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(
        val id: String,
        @Mapping(JdbcEntityFieldTypeResultMapper::class)
        @Mapping(JdbcEntityFieldTypeColumnMapper::class)
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

class JdbcEntityFieldTypeParameterMapper : JdbcParameterColumnMapper<JdbcMapperParameterRepository.Entity.FieldType> {
    override fun set(stmt: PreparedStatement, index: Int, value: JdbcMapperParameterRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER)
        } else {
            stmt.setInt(index, value.code)
        }
    }
}

@Repository
interface JdbcMapperParameterRepository : JdbcRepository {
    @EntityJdbc
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
    fun updateFieldType(id: String, @Mapping(JdbcEntityFieldTypeParameterMapper::class) fieldType: Entity.FieldType)
}

@EntityJdbc
data class JdbcEntityPart(val id: String, @field:Column("value1") val field1: Int)

class JdbcEntityPartRowMapper : JdbcRowMapper<JdbcEntityPart> {
    override fun apply(rs: ResultSet): JdbcEntityPart {
        return JdbcEntityPart(rs.getString(1), rs.getInt(2))
    }
}

@Repository
interface JdbcMapperRowRepository : JdbcRepository {
    @Mapping(JdbcEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<JdbcEntityPart>
}

class JdbcEntityPartResultSetMapper : JdbcResultSetMapper<Map<Int, List<JdbcEntityPart>>> {
    override fun apply(rs: ResultSet): Map<Int, List<JdbcEntityPart>> {
        val result = LinkedHashMap<Int, MutableList<JdbcEntityPart>>()
        while (rs.next()) {
            val entityPart = JdbcEntityPart(rs.getString(1), rs.getInt(2))
            result.computeIfAbsent(entityPart.field1) { ArrayList() }.add(entityPart)
        }
        return result
    }
}

@Repository
interface JdbcMapperResultSetRepository : JdbcRepository {
    @Mapping(JdbcEntityPartResultSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<JdbcEntityPart>>
}

@Module
interface JdbcJsonbMapperModule {
    @Json
    fun <T> jdbcJsonParameterColumnMapper(writer: JsonWriter<T>): JdbcParameterColumnMapper<T> {
        return JdbcParameterColumnMapper { stmt, index, value ->
            if (value == null) {
                stmt.setNull(index, Types.NULL)
            } else {
                val jsonb = PGobject()
                jsonb.type = "jsonb"
                jsonb.value = writer.toStringUnchecked(value)
                stmt.setObject(index, jsonb)
            }
        }
    }

    @Json
    fun <T> jdbcJsonResultColumnMapper(reader: JsonReader<T>): JdbcResultColumnMapper<T> {
        return JdbcResultColumnMapper { row, index ->
            val value = row.getString(index)
            if (value == null) null else reader.readUnchecked(value)
        }
    }
}

@Repository
interface JdbcJsonbRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(
        val id: UUID,
        @field:Column("value")
        @Json
        val value: JsonbValue
    ) {
        @Json
        data class JsonbValue(val name: String, val surname: String)
    }

    @Query("SELECT * FROM entities_jsonb WHERE id = :id")
    fun findById(id: UUID): Entity?

    @Query("INSERT INTO entities_jsonb(id, value) VALUES (:entity.id, :entity.value::jsonb)")
    fun insert(entity: Entity)
}

@Root
@Component
class RootService(
    private val jdbcCrudSyncRepository: JdbcCrudSyncRepository,
    private val jdbcCrudMacrosRepository: JdbcCrudMacrosRepository,
    private val jdbcIdRandomRepository: JdbcIdRandomRepository,
)

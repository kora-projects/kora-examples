package ru.tinkoff.kora.kotlin.example.jdbc

import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.common.annotation.Table
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import java.sql.PreparedStatement
import java.util.UUID

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
    fun insert(entity: JdbcMacrosEntity)

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

@Root
@Component
class RootService(
    private val jdbcCrudSyncRepository: JdbcCrudSyncRepository,
    private val jdbcCrudMacrosRepository: JdbcCrudMacrosRepository,
    private val jdbcIdRandomRepository: JdbcIdRandomRepository,
)

package ru.tinkoff.kora.kotlin.example.cassandra

import jakarta.annotation.Nullable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.cassandra.CassandraRepository
import ru.tinkoff.kora.database.cassandra.annotation.EntityCassandra
import ru.tinkoff.kora.database.cassandra.annotation.UDT
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

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

    @Query("TRUNCATE entities")
    fun deleteAll()
}

@Repository
interface CassandraCrudAsyncRepository : CassandraRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): CompletableFuture<CassandraEntity>

    @Query("SELECT * FROM entities")
    fun findAll(): CompletionStage<List<CassandraEntity>>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: CassandraEntity): CompletionStage<Void>

    @Query("TRUNCATE entities")
    fun deleteAll(): CompletableFuture<Void>
}

@Repository
interface CassandraCrudReactorRepository : CassandraRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): Mono<CassandraEntity>

    @Query("SELECT * FROM entities")
    fun findAll(): Flux<CassandraEntity>

    @Query("SELECT * FROM entities")
    fun findAllMono(): Mono<List<CassandraEntity>>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: CassandraEntity): Mono<Void>

    @Query("TRUNCATE entities")
    fun deleteAll(): Mono<Void>
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

@Root
@Component
class RootService(
    private val crudSyncRepository: CassandraCrudSyncRepository,
    private val cassandraUdtRepository: CassandraUdtRepository
)

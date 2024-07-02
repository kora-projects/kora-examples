package ru.tinkoff.kora.example.cassandra;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Batch;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Repository
public interface CassandraCrudAsyncRepository extends CassandraRepository {

    record Entity(String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    CompletionStage<Entity> findById(String id);

    @Query("SELECT * FROM entities")
    CompletionStage<List<Entity>> findAll();

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    CompletionStage<UpdateCount> insert(Entity entity);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    CompletionStage<UpdateCount> insertBatch(@Batch List<Entity> entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    CompletableFuture<UpdateCount> update(Entity entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    CompletableFuture<UpdateCount> updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    CompletableFuture<UpdateCount> deleteById(String id);

    @Query("TRUNCATE entities")
    CompletableFuture<UpdateCount> deleteAll();
}

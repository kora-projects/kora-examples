package ru.tinkoff.kora.example.cassandra;

import jakarta.annotation.Nullable;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;
import ru.tinkoff.kora.database.common.annotation.*;

@Repository
public interface CassandraCrudReactorRepository extends CassandraRepository {

    record Entity(String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    Mono<Entity> findById(String id);

    @Query("SELECT * FROM entities")
    Flux<Entity> findAll();

    @Query("SELECT * FROM entities")
    Mono<List<Entity>> findAllMono();

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    Mono<Void> insert(Entity entity);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    Mono<Void> insertBatch(@Batch List<Entity> entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    Mono<Void> update(Entity entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    Mono<Void> updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    Mono<Void> deleteById(String id);

    @Query("TRUNCATE entities")
    Mono<Void> deleteAll();
}

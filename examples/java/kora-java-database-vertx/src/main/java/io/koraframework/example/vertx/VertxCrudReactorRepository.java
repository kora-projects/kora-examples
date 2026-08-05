package io.koraframework.example.vertx;

import org.jspecify.annotations.Nullable;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.vertx.VertxRepository;

@Repository
public interface VertxCrudReactorRepository extends VertxRepository {

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
    Mono<UpdateCount> insertBatch(@Batch List<Entity> entity);

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
    Mono<UpdateCount> updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    Mono<Void> deleteById(String id);

    @Query("DELETE FROM entities")
    Mono<UpdateCount> deleteAll();
}

package ru.tinkoff.kora.example.r2dbc;

import jakarta.annotation.Nullable;
import java.util.List;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;

@Repository
public interface R2dbcIdSequenceRepository extends R2dbcRepository {

    record Entity(Long id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(null, name);
        }
    }

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    @Nullable
    Mono<Entity> findById(long id);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            RETURNING id
            """)
    Mono<Long> insert(Entity entity);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            """)
    @Id
    Mono<Long> createGenerated(Entity entity);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            """)
    @Id
    Mono<List<Long>> createGenerated(@Batch List<Entity> entity);
}

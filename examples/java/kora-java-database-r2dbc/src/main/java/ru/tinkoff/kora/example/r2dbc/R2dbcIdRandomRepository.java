package ru.tinkoff.kora.example.r2dbc;

import java.util.UUID;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;

@Repository
public interface R2dbcIdRandomRepository extends R2dbcRepository {

    record Entity(UUID id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(UUID.randomUUID(), name);
        }
    }

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    Mono<Entity> findById(UUID id);

    @Query("""
            INSERT INTO entities_uuid(id, name)
            VALUES (:entity.id, :entity.name)
            """)
    Mono<UpdateCount> insert(Entity entity);
}

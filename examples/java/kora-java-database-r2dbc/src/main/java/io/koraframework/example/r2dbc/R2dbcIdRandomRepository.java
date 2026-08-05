package io.koraframework.example.r2dbc;

import java.util.UUID;
import reactor.core.publisher.Mono;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.r2dbc.R2dbcRepository;

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

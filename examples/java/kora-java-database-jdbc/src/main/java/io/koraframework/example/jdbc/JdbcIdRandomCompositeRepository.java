package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdRandomCompositeRepository extends JdbcRepository {

    @EntityJdbc
    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(new EntityId(UUID.randomUUID(), UUID.randomUUID()), name);
        }

        public record EntityId(UUID a, UUID b) {}
    }

    @Query("SELECT * FROM entities_composite_uuid WHERE a = :id.a AND b = :id.b")
    @Nullable
    Entity findById(Entity.EntityId id);

    @Query("""
            INSERT INTO entities_composite_uuid(a, b, name)
            VALUES (:entity.id.a, :entity.id.b, :entity.name)
            """)
    UpdateCount insert(Entity entity);

    @Query("""
            INSERT INTO entities_composite_uuid(a, b, name)
            VALUES (:entity.id.a, :entity.id.b, :entity.name)
            """)
    UpdateCount insert(@Batch List<Entity> entity);
}

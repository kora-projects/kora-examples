package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdRandomCompositeRepository extends JdbcRepository {

    @Table("entities")
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

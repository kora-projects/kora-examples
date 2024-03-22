package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdSequenceCompositeRepository extends JdbcRepository {

    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(null, name);
        }

        public record EntityId(Long a, Long b) {}
    }

    @Query("SELECT * FROM entities_composite WHERE a = :id.a AND b = :id.b")
    @Nullable
    Entity findById(Entity.EntityId id);

    @Query("""
            INSERT INTO entities_composite(name)
            VALUES (:entity.name)
            """)
    UpdateCount insert(Entity entity);

    @Query("""
            INSERT INTO entities_composite(name)
            VALUES (:entity.name)
            """)
    @Id
    Entity.EntityId insertGenerated(Entity entity);

    @Query("""
            INSERT INTO entities_composite(name)
            VALUES (:entity.name)
            """)
    @Id
    List<Entity.EntityId> insertGenerated(@Batch List<Entity> entity);
}

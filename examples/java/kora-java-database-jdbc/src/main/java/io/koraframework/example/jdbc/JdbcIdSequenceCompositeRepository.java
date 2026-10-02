package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdSequenceCompositeRepository extends JdbcRepository {

    @EntityJdbc
    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(null, name);
        }

        @EntityJdbc
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

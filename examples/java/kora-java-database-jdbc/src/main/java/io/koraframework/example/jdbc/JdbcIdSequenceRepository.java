package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdSequenceRepository extends JdbcRepository {

    @EntityJdbc
    record Entity(@Id Long id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(null, name);
        }
    }

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    @Nullable
    Entity findById(long id);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            RETURNING id
            """)
    long insert(Entity entity);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            """)
    @Id
    Long insertGenerated(Entity entity);

    @Query("""
            INSERT INTO entities_sequence(name)
            VALUES (:entity.name)
            """)
    @Id
    List<Long> insertGenerated(@Batch List<Entity> entity);
}

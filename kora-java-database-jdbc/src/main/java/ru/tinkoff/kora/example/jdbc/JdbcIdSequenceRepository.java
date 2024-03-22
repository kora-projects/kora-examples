package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdSequenceRepository extends JdbcRepository {

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

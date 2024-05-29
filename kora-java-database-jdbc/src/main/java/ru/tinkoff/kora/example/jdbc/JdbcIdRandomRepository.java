package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.UUID;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdRandomRepository extends JdbcRepository {

    record Entity(UUID id,
                  @Column("name") String name) {

        public Entity(String name) {
            this(UUID.randomUUID(), name);
        }
    }

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    @Nullable
    Entity findById(UUID id);

    @Query("""
            INSERT INTO entities_uuid(id, name)
            VALUES (:entity.id, :entity.name)
            """)
    void insert(Entity entity);
}

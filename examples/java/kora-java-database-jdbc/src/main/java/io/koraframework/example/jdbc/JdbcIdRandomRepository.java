package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.util.UUID;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface JdbcIdRandomRepository extends JdbcRepository {

    @EntityJdbc
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

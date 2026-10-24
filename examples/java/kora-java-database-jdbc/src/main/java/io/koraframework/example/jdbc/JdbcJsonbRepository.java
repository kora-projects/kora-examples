package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.util.UUID;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.json.common.annotation.Json;

/**
 * @see JdbcJsonbMapperModule
 */
@Repository
public interface JdbcJsonbRepository extends JdbcRepository {

    @EntityJdbc
    record Entity(UUID id,
                  @Column("value") @Json JsonbValue value) {

        @Json
        public record JsonbValue(String name, String surname) {}
    }

    @Query("SELECT * FROM entities_jsonb WHERE id = :id")
    @Nullable
    Entity findById(UUID id);

    @Query("""
            INSERT INTO entities_jsonb(id, value)
            VALUES (:entity.id, :entity.value::jsonb)
            """)
    void insert(Entity entity);
}

package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.UUID;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.json.common.annotation.Json;

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

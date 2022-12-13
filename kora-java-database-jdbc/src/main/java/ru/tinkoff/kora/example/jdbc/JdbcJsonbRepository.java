package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.common.annotation.Json;

@Repository
public interface JdbcJsonbRepository extends JdbcRepository {

    final class JsonbResultMapper implements JdbcResultColumnMapper<Entity.JsonbValue> {

        private final JsonReader<Entity.JsonbValue> reader;

        public JsonbResultMapper(JsonReader<Entity.JsonbValue> reader) {
            this.reader = reader;
        }

        @Override
        public Entity.JsonbValue apply(ResultSet rs, int index) throws SQLException {
            try {
                var value = rs.getString(index);
                return reader.read(value);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    final class JsonbParameterMapper implements JdbcParameterColumnMapper<Entity.JsonbValue> {

        private final JsonWriter<Entity.JsonbValue> writer;

        public JsonbParameterMapper(JsonWriter<Entity.JsonbValue> writer) {
            this.writer = writer;
        }

        @Override
        public void set(PreparedStatement stmt, int index, @Nullable Entity.JsonbValue value) throws SQLException {
            try {
                if (value != null) {
                    stmt.setString(index, new String(writer.toByteArray(value), StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    record Entity(UUID id,
                  @Mapping(JsonbResultMapper.class)
                  @Mapping(JsonbParameterMapper.class)
                  @Column("value") JsonbValue value) {

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

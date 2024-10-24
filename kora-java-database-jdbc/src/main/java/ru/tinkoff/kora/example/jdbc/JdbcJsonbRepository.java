package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.postgresql.util.PGobject;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
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
            if (value != null) {
                PGobject jsonb = new PGobject();
                jsonb.setType("jsonb");
                jsonb.setValue(writer.toStringUnchecked(value));
                stmt.setObject(index, jsonb);
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
            VALUES (:entity.id, :entity.value)
            """)
    void insert(Entity entity);
}

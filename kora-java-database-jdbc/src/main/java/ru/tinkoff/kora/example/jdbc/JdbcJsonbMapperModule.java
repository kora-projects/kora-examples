package ru.tinkoff.kora.example.jdbc;

import java.sql.Types;
import org.postgresql.util.PGobject;
import ru.tinkoff.kora.common.Module;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.common.annotation.Json;

@Module
public interface JdbcJsonbMapperModule {

    @Json
    default <T> JdbcParameterColumnMapper<T> jdbcJsonParameterColumnMapper(JsonWriter<T> writer) {
        return (stmt, index, value) -> {
            if (value != null) {
                PGobject jsonb = new PGobject();
                jsonb.setType("jsonb");
                jsonb.setValue(writer.toStringUnchecked(value));
                stmt.setObject(index, jsonb);
            } else {
                stmt.setNull(index, Types.NULL);
            }
        };
    }

    @Json
    default <T> JdbcResultColumnMapper<T> jdbcJsonResultColumnMapper(JsonReader<T> reader) {
        return (row, index) -> {
            var value = row.getString(index);
            if (value == null) {
                return null;
            } else {
                return reader.readUnchecked(value);
            }
        };
    }
}

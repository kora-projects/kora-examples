package io.koraframework.example.jdbc;

import java.sql.Types;
import org.postgresql.util.PGobject;
import io.koraframework.common.annotation.Module;
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper;
import io.koraframework.json.common.JsonReader;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.annotation.Json;

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

package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper;

@Repository
public interface JdbcMapperColumnRepository extends JdbcRepository {

    final class EntityFieldTypeResultMapper implements JdbcResultColumnMapper<Entity.FieldType> {

        private static final Entity.FieldType[] ALL = Entity.FieldType.values();

        @Override
        public Entity.FieldType apply(ResultSet rs, int index) throws SQLException {
            var fieldAsInt = rs.getInt(index);
            for (var type : ALL) {
                if (type.code() == fieldAsInt) {
                    return type;
                }
            }

            return Entity.FieldType.UNKNOWN;
        }
    }

    final class EntityFieldTypeParameterMapper implements JdbcParameterColumnMapper<Entity.FieldType> {

        @Override
        public void set(PreparedStatement stmt, int index, @Nullable Entity.FieldType value) throws SQLException {
            if (value != null) {
                stmt.setInt(index, value.code());
            }
        }
    }

    @EntityJdbc
    record Entity(String id,
                  @Mapping(EntityFieldTypeResultMapper.class)
                  @Mapping(EntityFieldTypeParameterMapper.class)
                  @Column("value1") FieldType field1,
                  String value2,
                  @Nullable String value3) {

        public enum FieldType {

            UNKNOWN(-10),
            ONE(1),
            TWO(2);

            private final int code;

            FieldType(int code) {
                this.code = code;
            }

            public int code() {
                return code;
            }
        }
    }

    @Query("SELECT * FROM entities")
    List<Entity> findAll();
}

package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;

@Repository
public interface JdbcMapperParameterRepository extends JdbcRepository {

    final class EntityFieldTypeParameterMapper implements JdbcParameterColumnMapper<Entity.FieldType> {

        @Override
        public void set(PreparedStatement stmt, int index, @Nullable Entity.FieldType value)
                throws SQLException {
            if (value != null) {
                stmt.setInt(index, value.code());
            }
        }
    }

    record Entity(String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {

        enum FieldType {

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

    @Query("""
            UPDATE entities
            SET value1 = :fieldType
            WHERE id = :id
            """)
    void updateFieldType(String id,
                         @Mapping(EntityFieldTypeParameterMapper.class) Entity.FieldType fieldType);
}

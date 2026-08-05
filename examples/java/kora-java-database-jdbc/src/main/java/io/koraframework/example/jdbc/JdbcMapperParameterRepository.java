package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;

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

    @EntityJdbc
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

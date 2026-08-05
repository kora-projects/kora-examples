package io.koraframework.example.cassandra;

import com.datastax.oss.driver.api.core.data.SettableByName;
import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.mapper.parameter.CassandraParameterColumnMapper;
import io.koraframework.database.common.annotation.*;

@Repository
public interface CassandraMapperParameterRepository extends CassandraRepository {

    final class EntityFieldTypeParameterMapper implements CassandraParameterColumnMapper<Entity.FieldType> {

        @Override
        public void apply(SettableByName<?> stmt, int index, @Nullable Entity.FieldType value) {
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

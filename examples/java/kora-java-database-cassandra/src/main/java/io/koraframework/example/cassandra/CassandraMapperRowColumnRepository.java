package io.koraframework.example.cassandra;

import com.datastax.oss.driver.api.core.data.GettableByName;
import com.datastax.oss.driver.api.core.data.SettableByName;
import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.mapper.parameter.CassandraParameterColumnMapper;
import io.koraframework.database.cassandra.mapper.result.CassandraRowColumnMapper;
import io.koraframework.database.common.annotation.*;

@Repository
public interface CassandraMapperRowColumnRepository extends CassandraRepository {

    final class EntityFieldTypeResultMapper implements CassandraRowColumnMapper<Entity.FieldType> {

        private static final Entity.FieldType[] ALL = Entity.FieldType.values();

        @Override
        public Entity.FieldType apply(GettableByName row, int index) {
            var fieldAsInt = row.getInt(index);
            for (var type : ALL) {
                if (type.code() == fieldAsInt) {
                    return type;
                }
            }

            return Entity.FieldType.UNKNOWN;
        }
    }

    final class EntityFieldTypeParameterMapper implements
            CassandraParameterColumnMapper<CassandraMapperParameterRepository.Entity.FieldType> {

        @Override
        public void
                apply(SettableByName<?> stmt, int index, @Nullable CassandraMapperParameterRepository.Entity.FieldType value) {
            if (value != null) {
                stmt.setInt(index, value.code());
            }
        }
    }

    record Entity(String id,
                  @Mapping(EntityFieldTypeResultMapper.class)
                  @Mapping(EntityFieldTypeParameterMapper.class)
                  @Column("value1") FieldType field1,
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

    @Query("SELECT * FROM entities")
    List<Entity> findAll();
}

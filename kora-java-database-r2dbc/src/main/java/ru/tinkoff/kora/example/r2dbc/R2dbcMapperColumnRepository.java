package ru.tinkoff.kora.example.r2dbc;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper;
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper;

@Repository
public interface R2dbcMapperColumnRepository extends R2dbcRepository {

    final class EntityFieldTypeResultMapper implements R2dbcResultColumnMapper<Entity.FieldType> {

        private static final Entity.FieldType[] ALL = Entity.FieldType.values();

        @Nullable
        @Override
        public Entity.FieldType apply(Row row, String label) {
            var fieldAsInt = row.get(label, Integer.class);
            if (fieldAsInt == null) {
                return null;
            }

            for (var type : ALL) {
                if (type.code() == fieldAsInt) {
                    return type;
                }
            }

            return Entity.FieldType.UNKNOWN;
        }
    }

    final class EntityFieldTypeParameterMapper implements R2dbcParameterColumnMapper<Entity.FieldType> {

        @Override
        public void apply(Statement stmt, int index, @Nullable Entity.FieldType value) {
            if (value != null) {
                stmt.bind(index, value.code());
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
    Flux<Entity> findAll();
}

package io.koraframework.example.r2dbc;

import io.r2dbc.spi.Statement;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.r2dbc.R2dbcRepository;
import io.koraframework.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper;

@Repository
public interface R2dbcMapperParameterRepository extends R2dbcRepository {

    final class EntityFieldTypeParameterMapper implements R2dbcParameterColumnMapper<Entity.FieldType> {

        @Override
        public void apply(Statement stmt, int index, Entity.@Nullable FieldType value) {
            if (value != null) {
                stmt.bind(index, value.code());
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
    Mono<UpdateCount> updateFieldType(String id,
                                      @Mapping(EntityFieldTypeParameterMapper.class) Entity.FieldType fieldType);
}

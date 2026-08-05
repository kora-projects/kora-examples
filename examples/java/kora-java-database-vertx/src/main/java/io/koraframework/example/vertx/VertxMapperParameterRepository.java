package io.koraframework.example.vertx;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.vertx.VertxRepository;
import io.koraframework.database.vertx.mapper.parameter.VertxParameterColumnMapper;

@Repository
public interface VertxMapperParameterRepository extends VertxRepository {

    final class EntityFieldTypeParameterMapper implements VertxParameterColumnMapper<Entity.FieldType> {

        @Nullable
        @Override
        public Object apply(@Nullable Entity.FieldType fieldType) {
            return (fieldType == null)
                    ? null
                    : fieldType.code();
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
    UpdateCount updateFieldType(String id,
                                @Mapping(EntityFieldTypeParameterMapper.class) Entity.FieldType fieldType);
}

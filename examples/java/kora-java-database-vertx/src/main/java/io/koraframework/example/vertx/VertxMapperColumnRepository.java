package io.koraframework.example.vertx;

import io.vertx.sqlclient.Row;
import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.vertx.VertxRepository;
import io.koraframework.database.vertx.mapper.result.VertxResultColumnMapper;

@Repository
public interface VertxMapperColumnRepository extends VertxRepository {

    final class EntityFieldTypeResultMapper implements VertxResultColumnMapper<Entity.FieldType> {

        private static final Entity.FieldType[] ALL = Entity.FieldType.values();

        @Nullable
        @Override
        public Entity.FieldType apply(Row row, int index) {
            var fieldAsInt = row.get(Integer.class, index);
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

    record Entity(String id,
                  @Mapping(EntityFieldTypeResultMapper.class)
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

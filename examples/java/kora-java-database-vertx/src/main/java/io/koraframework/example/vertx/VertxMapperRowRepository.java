package io.koraframework.example.vertx;

import io.vertx.sqlclient.Row;
import java.util.List;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.vertx.VertxRepository;
import io.koraframework.database.vertx.mapper.result.VertxRowMapper;

@Repository
public interface VertxMapperRowRepository extends VertxRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartRowMapper implements VertxRowMapper<EntityPart> {

        @Override
        public EntityPart apply(Row row) {
            return new EntityPart(row.get(String.class, 0), row.get(Integer.class, 1));
        }
    }

    @Mapping(EntityPartRowMapper.class)
    @Query("SELECT id, value1 FROM entities")
    List<EntityPart> findAllParts();
}

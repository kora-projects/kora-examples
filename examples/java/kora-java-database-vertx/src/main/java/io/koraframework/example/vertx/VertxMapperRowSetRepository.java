package io.koraframework.example.vertx;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.vertx.VertxRepository;
import io.koraframework.database.vertx.mapper.result.VertxRowSetMapper;

@Repository
public interface VertxMapperRowSetRepository extends VertxRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartRowMapper implements VertxRowSetMapper<Map<Integer, List<EntityPart>>> {

        @Override
        public Map<Integer, List<EntityPart>> apply(RowSet<Row> rows) {
            var result = new LinkedHashMap<Integer, List<EntityPart>>(rows.size());
            for (Row row : rows) {
                var entityPart = new EntityPart(row.getString(0), row.getInteger(1));
                var entityParts = result.computeIfAbsent(entityPart.field1(), k -> new ArrayList<>());
                entityParts.add(entityPart);
            }
            return result;
        }
    }

    @Mapping(EntityPartRowMapper.class)
    @Query("SELECT id, value1 FROM entities")
    Map<Integer, List<EntityPart>> findAllParts();
}

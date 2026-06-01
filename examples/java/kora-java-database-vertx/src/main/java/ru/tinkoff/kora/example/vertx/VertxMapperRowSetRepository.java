package ru.tinkoff.kora.example.vertx;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.vertx.VertxRepository;
import ru.tinkoff.kora.database.vertx.mapper.result.VertxRowSetMapper;

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

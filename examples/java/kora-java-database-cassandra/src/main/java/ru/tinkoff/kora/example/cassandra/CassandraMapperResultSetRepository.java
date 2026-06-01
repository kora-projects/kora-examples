package ru.tinkoff.kora.example.cassandra;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;
import ru.tinkoff.kora.database.cassandra.mapper.result.CassandraResultSetMapper;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;

@Repository
public interface CassandraMapperResultSetRepository extends CassandraRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartResultSetMapper implements CassandraResultSetMapper<Map<Integer, List<EntityPart>>> {

        @Override
        public Map<Integer, List<EntityPart>> apply(ResultSet rows) {
            var result = new LinkedHashMap<Integer, List<EntityPart>>();
            for (Row row : rows.all()) {
                var entityPart = new EntityPart(row.getString(0), row.getInt(1));
                var entityParts = result.computeIfAbsent(entityPart.field1(), k -> new ArrayList<>());
                entityParts.add(entityPart);
            }
            return result;
        }
    }

    @Mapping(EntityPartResultSetMapper.class)
    @Query("SELECT id, value1 FROM entities")
    Map<Integer, List<EntityPart>> findAllParts();
}

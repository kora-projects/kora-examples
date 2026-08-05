package io.koraframework.example.cassandra;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.mapper.result.CassandraReactiveResultSetMapper;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;

@Repository
public interface CassandraMapperResultSetReactiveRepository extends CassandraRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartResultSetMapper implements
            CassandraReactiveResultSetMapper<Map<Integer, List<EntityPart>>, Mono<Map<Integer, List<EntityPart>>>> {

        @Override
        public Mono<Map<Integer, List<EntityPart>>> apply(ReactiveResultSet rows) {
            return Flux.from(rows)
                    .map(row -> new EntityPart(row.getString(0), row.getInt(1)))
                    .collect(HashMap::new, (collector, value) -> {
                        var parts = collector.computeIfAbsent(value.field1(), k -> new ArrayList<>());
                        parts.add(value);
                    });
        }
    }

    @Mapping(EntityPartResultSetMapper.class)
    @Query("SELECT id, value1 FROM entities")
    Mono<Map<Integer, List<EntityPart>>> findAllParts();
}

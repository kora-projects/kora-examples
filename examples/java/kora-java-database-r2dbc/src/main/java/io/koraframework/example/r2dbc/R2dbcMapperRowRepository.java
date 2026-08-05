package io.koraframework.example.r2dbc;

import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.r2dbc.R2dbcRepository;
import io.koraframework.database.r2dbc.mapper.result.R2dbcRowMapper;

@Repository
public interface R2dbcMapperRowRepository extends R2dbcRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartRowMapper implements R2dbcRowMapper<EntityPart> {

        @Override
        public EntityPart apply(Row row) {
            return new EntityPart(row.get(0, String.class), row.get(1, Integer.class));
        }
    }

    @Mapping(EntityPartRowMapper.class)
    @Query("SELECT id, value1 FROM entities")
    Flux<EntityPart> findAllParts();
}

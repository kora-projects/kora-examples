package ru.tinkoff.kora.example.r2dbc;

import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper;

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

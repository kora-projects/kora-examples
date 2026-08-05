package io.koraframework.example.cassandra;

import com.datastax.oss.driver.api.core.cql.Row;
import java.util.List;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.mapper.result.CassandraRowMapper;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;

@Repository
public interface CassandraMapperRowRepository extends CassandraRepository {

    record EntityPart(String id, int field1) {}

    final class EntityPartRowMapper implements CassandraRowMapper<EntityPart> {

        @Override
        public EntityPart apply(Row row) {
            return new EntityPart(row.getString(0), row.getInt(1));
        }
    }

    @Mapping(EntityPartRowMapper.class)
    @Query("SELECT id, value1 FROM entities")
    List<EntityPart> findAllParts();
}

package io.koraframework.example.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.database.jdbc.mapper.result.JdbcResultSetMapper;

@Repository
public interface JdbcMapperResultSetRepository extends JdbcRepository {

    @EntityJdbc
    record EntityPart(String id, int field1) {}

    final class EntityPartResultSetMapper implements JdbcResultSetMapper<Map<Integer, List<EntityPart>>> {

        @Override
        public Map<Integer, List<EntityPart>> apply(ResultSet rs) throws SQLException {
            var result = new LinkedHashMap<Integer, List<EntityPart>>();
            while (rs.next()) {
                var entityPart = new EntityPart(rs.getString(1), rs.getInt(2));
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

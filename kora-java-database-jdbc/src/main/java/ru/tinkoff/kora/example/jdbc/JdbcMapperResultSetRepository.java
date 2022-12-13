package ru.tinkoff.kora.example.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultSetMapper;

@Repository
public interface JdbcMapperResultSetRepository extends JdbcRepository {

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

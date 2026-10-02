package io.koraframework.example.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.database.jdbc.mapper.result.JdbcRowMapper;

@Repository
public interface JdbcMapperRowRepository extends JdbcRepository {

    @EntityJdbc
    record EntityPart(String id, int field1) {}

    final class EntityPartRowMapper implements JdbcRowMapper<EntityPart> {

        @Override
        public EntityPart apply(ResultSet rs) throws SQLException {
            return new EntityPart(rs.getString(1), rs.getInt(2));
        }
    }

    @Mapping(EntityPartRowMapper.class)
    @Query("SELECT id, value1 FROM entities")
    List<EntityPart> findAllParts();
}

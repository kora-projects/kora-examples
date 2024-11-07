package ru.tinkoff.kora.example.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcRowMapper;

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

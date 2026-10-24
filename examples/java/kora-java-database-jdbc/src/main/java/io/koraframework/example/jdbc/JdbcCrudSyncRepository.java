package io.koraframework.example.jdbc;

import org.jspecify.annotations.Nullable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Batch;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;

@Repository
public interface JdbcCrudSyncRepository extends JdbcRepository {

    @Component
    class ListOfStringJdbcParameterMapper implements JdbcParameterColumnMapper<List<String>> {

        @Override
        public void set(PreparedStatement stmt, int index, List<String> value) throws SQLException {
            Object[] objectArray = new Object[value.size()];
            Iterator<String> iterator = value.iterator();
            for (int i = 0; i < objectArray.length; i++) {
                objectArray[i] = iterator.next();
            }

            Array sqlArray = stmt.getConnection().createArrayOf("VARCHAR", objectArray);
            stmt.setArray(index, sqlArray);
        }
    }

    @EntityJdbc
    record Entity(String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    @Nullable
    Entity findById(String id);

    @Query("SELECT * FROM entities")
    List<Entity> findAll();

    @Query("SELECT * FROM entities WHERE id = ANY(:ids)")
    List<Entity> findAllByIds(@Mapping(ListOfStringJdbcParameterMapper.class) List<String> ids);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    void insert(Entity entity);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    UpdateCount insertBatch(@Batch List<Entity> entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    void update(Entity entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    UpdateCount updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM entities")
    UpdateCount deleteAll();
}

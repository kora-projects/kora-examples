package io.koraframework.example.cassandra;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.annotation.EntityCassandra;
import io.koraframework.database.common.annotation.*;

@Repository
public interface CassandraCrudSyncRepository extends CassandraRepository {

    @EntityCassandra
    record Entity(String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    @Nullable
    Entity findById(String id);

    @Query("SELECT * FROM entities")
    List<Entity> findAll();

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    void insert(Entity entity);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    void insertBatch(@Batch List<Entity> entity);

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
    void updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    void deleteById(String id);

    @Query("TRUNCATE entities")
    void deleteAll();
}

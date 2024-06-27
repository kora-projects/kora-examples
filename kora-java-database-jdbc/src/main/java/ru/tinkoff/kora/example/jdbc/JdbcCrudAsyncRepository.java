package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcCrudAsyncRepository extends JdbcRepository {

    @Table("entities")
    record Entity(@Id String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT * FROM entities WHERE id = :id")
    CompletionStage<Entity> findById(String id);

    @Query("SELECT * FROM entities")
    CompletionStage<List<Entity>> findAll();

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    CompletionStage<Void> insert(Entity entity);

    @Query("""
            INSERT INTO entities(id, value1, value2, value3)
            VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)
            """)
    CompletionStage<UpdateCount> insertBatch(@Batch List<Entity> entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    CompletionStage<Void> update(Entity entity);

    @Query("""
            UPDATE entities
            SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3
            WHERE id = :entity.id
            """)
    CompletionStage<UpdateCount> updateBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    CompletionStage<Void> deleteById(String id);

    @Query("DELETE FROM entities")
    CompletionStage<UpdateCount> deleteAll();
}

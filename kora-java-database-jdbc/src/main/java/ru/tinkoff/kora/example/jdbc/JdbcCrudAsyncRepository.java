package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Batch;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcCrudAsyncRepository extends JdbcRepository {

    class Entity {

        private String id;
        @Column("value1")
        private int field1;
        private String value2;
        @Nullable
        private String value3;

        public Entity() {}

        public Entity(String id, int field1, String value2, @Nullable String value3) {
            this.id = id;
            this.field1 = field1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getField1() {
            return field1;
        }

        public void setField1(int field1) {
            this.field1 = field1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }

        @Nullable
        public String getValue3() {
            return value3;
        }

        public void setValue3(@Nullable String value3) {
            this.value3 = value3;
        }
    }

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

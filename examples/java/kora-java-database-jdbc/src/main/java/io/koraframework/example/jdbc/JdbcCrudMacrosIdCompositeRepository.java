package io.koraframework.example.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.jdbc.JdbcCrudMacrosIdCompositeRepository.Entity.EntityId;

@Repository
public interface JdbcCrudMacrosIdCompositeRepository extends JdbcRepository {

    @EntityJdbc
    @Table("entities_composite_uuid")
    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public record EntityId(UUID a, UUID b) {

            public EntityId() {
                this(UUID.randomUUID(), UUID.randomUUID());
            }
        }
    }

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE %{id#where}")
    Optional<Entity> findById(EntityId id);

    @Query("SELECT %{return#selects} FROM %{return#table}")
    List<Entity> findAll();

    @Query("INSERT INTO %{entity#inserts}")
    UpdateCount insert(Entity entity);

    @Query("INSERT INTO %{entity#inserts}")
    UpdateCount insertBatch(@Batch List<Entity> entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount update(Entity entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount updateBatch(@Batch List<Entity> entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (a, b) DO UPDATE SET %{entity#updates}")
    UpdateCount upsert(Entity entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (a, b) DO UPDATE SET %{entity#updates}")
    UpdateCount upsertBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    UpdateCount deleteById(EntityId id);

    @Query("DELETE FROM entities_composite_uuid")
    UpdateCount deleteAll();
}

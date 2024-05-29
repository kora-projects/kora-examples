package ru.tinkoff.kora.example.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.example.jdbc.JdbcCrudMacrosIdCompositeRepository.Entity.EntityId;

@Repository
public interface JdbcCrudMacrosIdCompositeRepository extends JdbcRepository {

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

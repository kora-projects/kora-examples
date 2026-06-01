package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

@Repository
public interface JdbcCrudMacrosRepository extends JdbcRepository {

    @EntityJdbc
    @Table("entities")
    record Entity(@Id String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<Entity> findById(String id);

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

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    UpdateCount upsert(Entity entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    UpdateCount upsertBatch(@Batch List<Entity> entity);

    @Query("DELETE FROM entities WHERE id = :id")
    UpdateCount deleteById(String id);

    @Query("DELETE FROM entities")
    UpdateCount deleteAll();
}

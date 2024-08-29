package ru.tinkoff.kora.example.jdbc;

import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Batch;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;

public interface AbstractJdbcCrudRepository<K, V> extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table}")
    List<V> findAll();

    @Query("INSERT INTO %{entity#inserts}")
    UpdateCount insert(V entity);

    @Query("INSERT INTO %{entity#inserts}")
    UpdateCount insertBatch(@Batch List<V> entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount update(V entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount updateBatch(@Batch List<V> entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    UpdateCount upsert(V entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    UpdateCount upsertBatch(@Batch List<V> entity);

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    UpdateCount delete(V entity);

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    UpdateCount deleteBatch(@Batch List<V> entity);
}

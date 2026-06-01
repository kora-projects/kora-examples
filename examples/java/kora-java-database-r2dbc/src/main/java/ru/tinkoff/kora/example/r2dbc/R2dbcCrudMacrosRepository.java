package ru.tinkoff.kora.example.r2dbc;

import jakarta.annotation.Nullable;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;

@Repository
public interface R2dbcCrudMacrosRepository extends R2dbcRepository {

    @Table("entities")
    record Entity(@Id String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Mono<Entity> findById(String id);

    @Query("SELECT %{return#selects} FROM %{return#table}")
    Flux<Entity> findAll();

    @Query("SELECT %{return#selects} FROM %{return#table}")
    Mono<List<Entity>> findAllMono();

    @Query("INSERT INTO %{entity#inserts}")
    Mono<Void> insert(Entity entity);

    @Query("INSERT INTO %{entity#inserts}")
    Mono<UpdateCount> insertBatch(@Batch List<Entity> entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    Mono<Void> update(Entity entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    Mono<UpdateCount> updateBatch(@Batch List<Entity> entity);

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    Mono<UpdateCount> upsert(Entity entity);

    @Query("DELETE FROM entities WHERE id = :id")
    Mono<Void> deleteById(String id);

    @Query("DELETE FROM entities")
    Mono<UpdateCount> deleteAll();
}

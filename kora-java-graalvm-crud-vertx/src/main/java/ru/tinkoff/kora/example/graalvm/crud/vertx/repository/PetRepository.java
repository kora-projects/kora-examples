package ru.tinkoff.kora.example.graalvm.crud.vertx.repository;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.vertx.VertxRepository;
import ru.tinkoff.kora.example.graalvm.crud.vertx.model.dao.Pet;
import ru.tinkoff.kora.example.graalvm.crud.vertx.model.dao.PetWithCategory;

@Repository
public interface PetRepository extends VertxRepository {

    @Query("""
            SELECT p.id, p.name, p.status, p.category_id, c.name as category_name
            FROM pets p
            JOIN categories c on c.id = p.category_id
            WHERE p.id = :id
            """)
    Mono<PetWithCategory> findById(long id);

    @Query("INSERT INTO %{entity#inserts -= id} RETURNING id")
    Mono<Long> insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    Mono<Void> update(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    Mono<UpdateCount> deleteById(long id);
}

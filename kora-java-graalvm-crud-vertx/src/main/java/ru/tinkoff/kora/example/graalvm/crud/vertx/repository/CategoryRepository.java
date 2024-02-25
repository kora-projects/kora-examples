package ru.tinkoff.kora.example.graalvm.crud.vertx.repository;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.vertx.VertxRepository;
import ru.tinkoff.kora.example.graalvm.crud.vertx.model.dao.PetCategory;

@Repository
public interface CategoryRepository extends VertxRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE name = :name")
    Mono<PetCategory> findByName(String name);

    @Query("INSERT INTO categories(name) VALUES (:categoryName) RETURNING id")
    Mono<Long> insert(String categoryName);

    @Query("DELETE FROM categories WHERE id = :id")
    Mono<UpdateCount> deleteById(long id);
}

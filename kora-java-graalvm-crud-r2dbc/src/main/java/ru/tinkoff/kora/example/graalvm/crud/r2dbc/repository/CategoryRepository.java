package ru.tinkoff.kora.example.graalvm.crud.r2dbc.repository;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetCategory;

@Repository
public interface CategoryRepository extends R2dbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE name = :name")
    Mono<PetCategory> findByName(String name);

    @Id
    @Query("INSERT INTO categories(name) VALUES (:categoryName)")
    Mono<Long> insert(String categoryName);

    @Query("DELETE FROM categories WHERE id = :id")
    Mono<UpdateCount> deleteById(long id);
}

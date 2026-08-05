package io.koraframework.example.graalvm.crud.vertx.repository;

import reactor.core.publisher.Mono;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.vertx.VertxRepository;
import io.koraframework.example.graalvm.crud.vertx.model.dao.PetCategory;

@Repository
public interface CategoryRepository extends VertxRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE name = :name")
    Mono<PetCategory> findByName(String name);

    @Query("INSERT INTO categories(name) VALUES (:categoryName) RETURNING id")
    Mono<Long> insert(String categoryName);

    @Query("DELETE FROM categories WHERE id = :id")
    Mono<UpdateCount> deleteById(long id);
}

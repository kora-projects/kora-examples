package io.koraframework.example.graalvm.crud.r2dbc.repository;

import reactor.core.publisher.Mono;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.r2dbc.R2dbcRepository;
import io.koraframework.example.graalvm.crud.r2dbc.model.dao.Pet;
import io.koraframework.example.graalvm.crud.r2dbc.model.dao.PetWithCategory;

@Repository
public interface PetRepository extends R2dbcRepository {

    @Query("""
            SELECT p.id, p.name, p.status, p.category_id, c.name as category_name
            FROM pets p
            JOIN categories c on c.id = p.category_id
            WHERE p.id = :id
            """)
    Mono<PetWithCategory> findById(long id);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    Mono<Long> insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    Mono<Void> update(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    Mono<UpdateCount> deleteById(long id);
}

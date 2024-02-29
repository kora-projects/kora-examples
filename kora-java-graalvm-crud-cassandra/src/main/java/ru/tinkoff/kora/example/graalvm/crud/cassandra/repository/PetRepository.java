package ru.tinkoff.kora.example.graalvm.crud.cassandra.repository;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;

@Repository
public interface PetRepository extends CassandraRepository {

    @Query("""
            SELECT %{return#selects}
            FROM %{return#table}
            WHERE id = :id
            """)
    Mono<Pet> findById(long id);

    @Query("INSERT INTO %{entity#inserts}")
    Mono<Void> insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    Mono<Void> update(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    Mono<Void> deleteById(long id);
}

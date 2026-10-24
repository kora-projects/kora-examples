package io.koraframework.example.graalvm.crud.cassandra.repository;

import org.jspecify.annotations.Nullable;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.example.graalvm.crud.cassandra.model.dao.Pet;

@Repository
public interface PetRepository extends CassandraRepository {

    @Query("""
            SELECT %{return#selects}
            FROM %{return#table}
            WHERE id = :id
            """)
    @Nullable
    Pet findById(long id);

    @Query("INSERT INTO %{entity#inserts}")
    void insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    void update(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    void deleteById(long id);
}

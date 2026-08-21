package io.koraframework.example.petclinic.repository;

import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.petclinic.model.dao.Pet;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<Pet> findById(long id);

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE owner_id = :ownerId ORDER BY name")
    List<Pet> findByOwnerId(long ownerId);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount update(Pet entity);
}

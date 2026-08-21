package io.koraframework.example.petclinic.repository;

import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.petclinic.model.dao.Owner;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<Owner> findById(long id);

    @Query("""
            SELECT %{return#selects}
            FROM %{return#table}
            WHERE lower(last_name) LIKE lower(:lastNamePattern)
            ORDER BY last_name, first_name
            """)
    List<Owner> findByLastName(String lastNamePattern);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Owner entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    UpdateCount update(Owner entity);
}

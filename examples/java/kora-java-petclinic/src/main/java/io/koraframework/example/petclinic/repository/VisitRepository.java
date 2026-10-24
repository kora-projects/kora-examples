package io.koraframework.example.petclinic.repository;

import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.petclinic.model.dao.Visit;
import java.util.List;

@Repository
public interface VisitRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE pet_id = :petId ORDER BY visit_date, id")
    List<Visit> findByPetId(long petId);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Visit entity);
}

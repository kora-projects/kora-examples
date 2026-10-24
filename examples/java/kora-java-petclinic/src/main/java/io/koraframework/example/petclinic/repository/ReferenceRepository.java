package io.koraframework.example.petclinic.repository;

import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.petclinic.model.dao.PetType;
import io.koraframework.example.petclinic.model.dao.VetWithSpecialties;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferenceRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} ORDER BY name")
    List<PetType> findPetTypes();

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<PetType> findPetType(long id);

    @Query("""
            SELECT %{return.vet#selects}, %{return.specialties#selects}
            FROM %{return.vet#table as v}
            LEFT JOIN vet_specialties vs ON vs.vet_id = v.id
            LEFT JOIN %{return.specialties#table as s} ON s.id = vs.specialty_id
            ORDER BY v.last_name, v.first_name, s.name
            """)
    List<VetWithSpecialties> findVets();
}

package io.koraframework.kotlin.example.petclinic.repository

import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.petclinic.model.PetType
import io.koraframework.kotlin.example.petclinic.model.VetWithSpecialties

@Repository
interface ReferenceRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} ORDER BY name")
    fun findPetTypes(): List<PetType>

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findPetType(id: Long): PetType?

    @Query("""
        SELECT %{return.vet#selects}, %{return.specialties#selects}
        FROM %{return.vet#table as v}
        LEFT JOIN vet_specialties vs ON vs.vet_id = v.id
        LEFT JOIN %{return.specialties#table as s} ON s.id = vs.specialty_id
        ORDER BY v.last_name, v.first_name, s.name
    """)
    fun findVets(): List<VetWithSpecialties>
}

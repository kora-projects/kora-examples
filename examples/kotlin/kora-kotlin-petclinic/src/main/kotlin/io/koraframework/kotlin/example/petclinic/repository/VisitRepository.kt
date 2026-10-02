package io.koraframework.kotlin.example.petclinic.repository

import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.petclinic.model.Visit

@Repository
interface VisitRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE pet_id = :petId ORDER BY visit_date, id")
    fun findByPetId(petId: Long): List<Visit>

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    fun insert(entity: Visit): Long
}

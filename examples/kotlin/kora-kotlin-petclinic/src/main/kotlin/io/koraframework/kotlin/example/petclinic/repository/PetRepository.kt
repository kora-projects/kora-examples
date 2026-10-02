package io.koraframework.kotlin.example.petclinic.repository

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.petclinic.model.Pet

@Repository
interface PetRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: Long): Pet?

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE owner_id = :ownerId ORDER BY name")
    fun findByOwnerId(ownerId: Long): List<Pet>

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    fun insert(entity: Pet): Long

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Pet): UpdateCount
}

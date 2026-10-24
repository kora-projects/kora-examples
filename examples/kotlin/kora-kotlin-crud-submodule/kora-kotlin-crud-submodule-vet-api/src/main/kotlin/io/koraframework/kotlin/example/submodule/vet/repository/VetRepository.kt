package io.koraframework.kotlin.example.submodule.vet.repository

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.submodule.vet.model.dao.Vet

@Repository
interface VetRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<Vet>

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: Long): Vet?

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    fun insert(entity: Vet): Long

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Vet)

    @Query("DELETE FROM vets WHERE id = :id")
    fun deleteById(id: Long): UpdateCount
}

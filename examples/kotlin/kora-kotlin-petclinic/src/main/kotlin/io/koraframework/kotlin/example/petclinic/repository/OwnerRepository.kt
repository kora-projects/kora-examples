package io.koraframework.kotlin.example.petclinic.repository

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.petclinic.model.Owner

@Repository
interface OwnerRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: Long): Owner?

    @Query("""
        SELECT %{return#selects}
        FROM %{return#table}
        WHERE lower(last_name) LIKE lower(:lastNamePattern)
        ORDER BY last_name, first_name
    """)
    fun findByLastName(lastNamePattern: String): List<Owner>

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    fun insert(entity: Owner): Long

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Owner): UpdateCount
}

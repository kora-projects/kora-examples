package io.koraframework.kotlin.example.submodule.pet.repository

import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetCategory

@Repository
interface CategoryRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE name = :name")
    fun findByName(name: String): PetCategory?

    @Id
    @Query("INSERT INTO categories(name) VALUES (:categoryName)")
    fun insert(categoryName: String): Long

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteById(id: Long)
}

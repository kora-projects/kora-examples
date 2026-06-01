package ru.tinkoff.kora.kotlin.example.crud.repository

import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.kotlin.example.crud.model.PetCategory

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

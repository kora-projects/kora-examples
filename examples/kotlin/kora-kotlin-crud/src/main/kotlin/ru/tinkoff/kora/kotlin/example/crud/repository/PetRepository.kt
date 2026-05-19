package ru.tinkoff.kora.kotlin.example.crud.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.kotlin.example.crud.model.Pet
import ru.tinkoff.kora.kotlin.example.crud.model.PetWithCategory

@Repository
interface PetRepository : JdbcRepository {

    @Query(
        """
        SELECT p.id, p.name, p.status, p.category_id, c.name as category_name 
        FROM pets p 
        JOIN categories c on c.id = p.category_id 
        WHERE p.id = :id
        """
    )
    fun findById(id: Long): PetWithCategory?

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    fun insert(entity: Pet): Int

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Pet)

    @Query("DELETE FROM pets WHERE id = :id")
    fun deleteById(id: Long): UpdateCount
}

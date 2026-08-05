package io.koraframework.kotlin.example.submodule.pet.repository

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.submodule.pet.model.dao.Pet
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetWithCategory

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
    fun insert(entity: Pet): Long

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: Pet)

    @Query("DELETE FROM pets WHERE id = :id")
    fun deleteById(id: Long): UpdateCount
}

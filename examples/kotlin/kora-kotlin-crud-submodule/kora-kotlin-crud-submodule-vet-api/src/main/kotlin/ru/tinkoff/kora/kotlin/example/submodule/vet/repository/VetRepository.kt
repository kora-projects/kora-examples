package ru.tinkoff.kora.kotlin.example.submodule.vet.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.kotlin.example.submodule.vet.model.dao.Vet

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

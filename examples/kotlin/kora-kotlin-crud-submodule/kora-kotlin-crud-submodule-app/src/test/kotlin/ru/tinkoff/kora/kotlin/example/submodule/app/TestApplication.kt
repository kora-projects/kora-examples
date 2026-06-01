package ru.tinkoff.kora.kotlin.example.submodule.app

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.PetCategory

@KoraApp
interface TestApplication : Application {
    @Root
    @Component
    @Repository
    interface TestPetRepository : JdbcRepository {
        @Query("SELECT %{return#selects} FROM %{return#table}")
        fun findAll(): List<Pet>

        @Query("DELETE FROM pets")
        fun deleteAll()
    }

    @Root
    @Component
    @Repository
    interface TestCategoryRepository : JdbcRepository {
        @Query("SELECT %{return#selects} FROM %{return#table}")
        fun findAll(): List<PetCategory>

        @Query("DELETE FROM categories")
        fun deleteAll()
    }
}

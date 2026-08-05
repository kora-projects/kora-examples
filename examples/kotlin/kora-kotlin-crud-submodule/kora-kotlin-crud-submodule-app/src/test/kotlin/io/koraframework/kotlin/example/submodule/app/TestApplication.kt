package io.koraframework.kotlin.example.submodule.app

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.kotlin.example.submodule.pet.model.dao.Pet
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetCategory

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

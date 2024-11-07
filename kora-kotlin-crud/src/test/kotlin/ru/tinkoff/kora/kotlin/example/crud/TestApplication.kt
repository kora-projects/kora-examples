package ru.tinkoff.kora.kotlin.example.crud

import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.kotlin.example.crud.model.Pet
import ru.tinkoff.kora.kotlin.example.crud.model.PetCategory

/**
 * Тестовый контейнер приложение, который расширяет основное приложение и например добавляет некоторые компоненты
 * из общих модулей, которые не используются в данном приложении, которые могут быть например использованы в других подобных приложениях.
 * Например, когда у вас есть разные приложения READ API и WRITE API.
 * Либо, вам нужны некоторые функции сохранения/удаления/обновления только для тестирования в качестве быстрой тестовой утилиты.
 * <p>
 * Но мы НАСТОЯТЕЛЬНО РЕКОМЕНДУЕМ ИСПОЛЬЗОВАТЬ тестирование приложения как черный ящик в качестве основного инструмента тестирования.
 * -------
 * Test Application than extends Real Application and may be adds some components
 * from common modules that are not used in Real App, but may be used in other similar apps.
 * Like when you have different READ API application and WRITE API application
 * or may be, you need some save/delete/update methods only for testing as fast test utils.
 * <p>
 * But we STRONGLY ENCOURAGE AND RECOMMEND TO USE black box testing as a primary source of truth for tests.
 */
@KoraApp
interface TestApplication : Application {

    @Repository
    interface TestPetRepository : JdbcRepository {
        @Query("SELECT %{return#selects} FROM %{return#table}")
        fun findAll(): List<Pet>

        @Query("DELETE FROM pets")
        fun deleteAll()
    }

    @Repository
    interface TestCategoryRepository : JdbcRepository {
        @Query("SELECT %{return#selects} FROM %{return#table}")
        fun findAll(): List<PetCategory>

        @Query("DELETE FROM categories")
        fun deleteAll()
    }

    // Need any fake root to require components to include them in graph
    @Root
    @Tag(TestApplication::class)
    fun testRoot(
        petRepository: TestPetRepository,
        categoryRepository: TestCategoryRepository
    ): String = "root"
}

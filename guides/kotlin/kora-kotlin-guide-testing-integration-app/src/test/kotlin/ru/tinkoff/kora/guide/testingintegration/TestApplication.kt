package ru.tinkoff.kora.guide.testingintegration

import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.guide.databasejdbc.Application
import ru.tinkoff.kora.guide.databasejdbc.repository.UserDAO

@KoraApp
interface TestApplication : Application {

    @Repository
    interface TestUserRepository : JdbcRepository {

        @Query("SELECT id, name, email, created_at FROM users ORDER BY id")
        fun findAll(): List<UserDAO>

        @Query("DELETE FROM users")
        fun deleteAll()
    }

    @Tag(TestApplication::class)
    @Root
    fun testRoot(ignored: TestUserRepository): String = "test-root"
}

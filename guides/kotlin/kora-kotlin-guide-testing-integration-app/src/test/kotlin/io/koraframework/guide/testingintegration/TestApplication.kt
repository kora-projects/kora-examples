package io.koraframework.guide.testingintegration

import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.guide.databasejdbc.Application
import io.koraframework.guide.databasejdbc.repository.UserDAO

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

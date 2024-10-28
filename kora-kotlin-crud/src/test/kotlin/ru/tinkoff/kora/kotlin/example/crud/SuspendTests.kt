package ru.tinkoff.kora.kotlin.example.crud

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.Network
import io.goodforgod.testcontainers.extensions.jdbc.ConnectionPostgreSQL
import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.goodforgod.testcontainers.extensions.jdbc.Migration
import io.goodforgod.testcontainers.extensions.jdbc.TestcontainersPostgreSQL
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.database.jdbc.*
import ru.tinkoff.kora.database.jdbc.JdbcHelper.SqlRunnable
import ru.tinkoff.kora.kotlin.example.crud.repository.CategoryRepository
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersPostgreSQL(
    network = Network(shared = true),
    mode = ContainerMode.PER_RUN,
    migration = Migration(
        engine = Migration.Engines.FLYWAY,
        apply = Migration.Mode.PER_METHOD,
        drop = Migration.Mode.PER_METHOD
    )
)
@KoraAppTest(Application::class)
class SuspendTests(
    @ConnectionPostgreSQL val connection: JdbcConnection
) : KoraAppTestConfigModifier {

    @TestComponent
    lateinit var categoryRepository: CategoryRepository

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        db {
          jdbcUrl = "${connection.params().jdbcUrl()}"
          username = "${connection.params().username()}"
          password = "${connection.params().password()}"
          poolName = "kora"
          telemetry.logging.enabled = true
        }
    """.trimIndent()
    )

    @Test
    fun check1() {
        try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    categoryRepository.jdbcConnectionFactory.inTx(SqlRunnable {
                        runBlocking {
                            async { categoryRepository.insertSuspend("Foo") }
                            async { categoryRepository.insertSuspend("Bar") }
                            async { categoryRepository.insertSuspend("Baz") }

                            if (true) {
                                throw IllegalStateException("OPS")
                            }
                        }
                    })
                }
            }

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check2() {
        try {
            categoryRepository.jdbcConnectionFactory.inTx(
                SqlRunnable {
                    categoryRepository.insert("Foo")
                    categoryRepository.insert("Bar")
                    categoryRepository.insert("Baz")

                    if (true) {
                        throw IllegalStateException("OPS")
                    }
                }
            )

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check3() {
        try {
            categoryRepository.jdbcConnectionFactory.inTx(
                SqlRunnable {
                    categoryRepository.insert("Foo")
                    categoryRepository.insert("Bar")
                    categoryRepository.insert("Baz")

                    if (true) {
                        throw IllegalStateException("OPS")
                    }
                }
            )

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check4() {
        try {
            runBlocking {
                categoryRepository.jdbcConnectionFactory.inTxSuspend {
                    categoryRepository.insertSuspend("Foo")
                    categoryRepository.insertSuspend("Bar")
                    categoryRepository.insertSuspend("Baz")

                    if (true) {
                        throw IllegalStateException("OPS")
                    }
                }
            }

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check5() {
        try {
            runBlocking {
                GlobalScope.async {
                    categoryRepository.jdbcConnectionFactory.inTxSuspend(Dispatchers.IO) {
                        categoryRepository.insertSuspend("Foo")
                        categoryRepository.insertSuspend("Bar")
                        categoryRepository.insertSuspend("Baz")

                        if (true) {
                            throw IllegalStateException("OPS")
                        }
                    }
                }.await()
            }

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check6() {
        try {
            runBlocking {
                categoryRepository.jdbcConnectionFactory.inTxSuspendContext {
                    categoryRepository.insertSuspend("Foo")
                    categoryRepository.insertSuspend("Bar")
                    categoryRepository.insertSuspend("Baz")

                    if (true) {
                        throw IllegalStateException("OPS")
                    }
                }
            }

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }

    @Test
    fun check7() {
        try {
            runBlocking {
                GlobalScope.async {
                    categoryRepository.jdbcConnectionFactory.inTxSuspendContext(Dispatchers.IO) {
                        categoryRepository.insertSuspend("Foo")
                        categoryRepository.insertSuspend("Bar")
                        categoryRepository.insertSuspend("Baz")

                        if (true) {
                            throw IllegalStateException("OPS")
                        }
                    }
                }.await()
            }

            fail<Unit>("OPS")
        } catch (e: Exception) {
            assertEquals("OPS", e.message)
        }

        assertNull(categoryRepository.findByName("Foo"))
        assertNull(categoryRepository.findByName("Bar"))
        assertNull(categoryRepository.findByName("Baz"))
    }
}

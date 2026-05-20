package ru.tinkoff.kora.kotlin.example.jdbc

import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification

fun jdbcConfig(connection: JdbcConnection): KoraConfigModification {
    val params = connection.params()
    return KoraConfigModification.ofSystemProperty("POSTGRES_JDBC_URL", params.jdbcUrl())
        .withSystemProperty("POSTGRES_USER", params.username())
        .withSystemProperty("POSTGRES_PASS", params.password())
}

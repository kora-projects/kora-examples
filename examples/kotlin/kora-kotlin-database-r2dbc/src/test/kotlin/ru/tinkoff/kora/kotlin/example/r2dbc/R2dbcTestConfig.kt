package ru.tinkoff.kora.kotlin.example.r2dbc

import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification

fun r2dbcConfig(connection: JdbcConnection): KoraConfigModification {
    val params = connection.params()
    val url = "r2dbc:postgresql://${params.host()}:${params.port()}/${params.database()}"
    return KoraConfigModification.ofSystemProperty("POSTGRES_R2DBC_URL", url)
        .withSystemProperty("POSTGRES_USER", params.username())
        .withSystemProperty("POSTGRES_PASS", params.password())
}

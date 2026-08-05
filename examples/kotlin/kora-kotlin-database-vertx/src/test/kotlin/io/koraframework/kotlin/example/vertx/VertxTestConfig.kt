package io.koraframework.kotlin.example.vertx

import io.goodforgod.testcontainers.extensions.jdbc.JdbcConnection
import io.koraframework.test.extension.junit5.KoraConfigModification

fun vertxConfig(connection: JdbcConnection): KoraConfigModification {
    val params = connection.params()
    return KoraConfigModification
        .ofSystemProperty("POSTGRES_VERTX_URI", "postgresql://${params.host()}:${params.port()}/${params.database()}")
        .withSystemProperty("POSTGRES_USER", params.username())
        .withSystemProperty("POSTGRES_PASS", params.password())
}

package ru.tinkoff.kora.guide.databasejdbc.advanced

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.flyway.FlywayJdbcDatabaseModule
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    JdbcDatabaseModule,
    FlywayJdbcDatabaseModule,
    UndertowHttpServerModule

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

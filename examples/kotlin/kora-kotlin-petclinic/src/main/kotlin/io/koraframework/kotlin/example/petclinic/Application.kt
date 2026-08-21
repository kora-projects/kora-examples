package io.koraframework.kotlin.example.petclinic

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.flyway.FlywayJdbcDatabaseModule
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.module.ValidationModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JdbcDatabaseModule,
    FlywayJdbcDatabaseModule, ValidationModule, JsonModule, UndertowPublicHttpServerModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

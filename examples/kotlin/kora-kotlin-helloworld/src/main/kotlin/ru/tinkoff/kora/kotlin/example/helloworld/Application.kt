package ru.tinkoff.kora.kotlin.example.helloworld

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule


@KoraApp
interface Application : HoconConfigModule,
    JsonModule,
    LogbackModule,
    UndertowHttpServerModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

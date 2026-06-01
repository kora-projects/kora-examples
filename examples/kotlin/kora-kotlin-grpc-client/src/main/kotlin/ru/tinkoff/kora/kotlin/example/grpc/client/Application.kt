package ru.tinkoff.kora.kotlin.example.grpc.client

import ru.tinkoff.grpc.client.GrpcClientModule
import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, GrpcClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

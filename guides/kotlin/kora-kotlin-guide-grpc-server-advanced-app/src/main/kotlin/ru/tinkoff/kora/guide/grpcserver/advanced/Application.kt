package ru.tinkoff.kora.guide.grpcserver.advanced

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.grpc.server.GrpcServerModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, GrpcServerModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

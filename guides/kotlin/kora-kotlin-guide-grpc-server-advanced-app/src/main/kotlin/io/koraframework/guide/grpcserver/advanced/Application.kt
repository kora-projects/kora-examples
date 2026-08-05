package io.koraframework.guide.grpcserver.advanced

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.grpc.server.GrpcServerModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, GrpcServerModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

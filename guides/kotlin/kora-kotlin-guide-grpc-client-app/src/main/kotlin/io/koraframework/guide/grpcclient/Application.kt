package io.koraframework.guide.grpcclient

import ru.tinkoff.grpc.client.GrpcClientModule
import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    GrpcClientModule,
    UndertowPublicHttpServerModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

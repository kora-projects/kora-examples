package io.koraframework.guide.resilient

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.resilient.ResilientModule

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowPublicHttpServerModule,
    JsonModule,
    LogbackModule,
    ResilientModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

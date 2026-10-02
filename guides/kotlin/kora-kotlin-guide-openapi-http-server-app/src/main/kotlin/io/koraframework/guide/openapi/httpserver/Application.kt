package io.koraframework.guide.openapi.httpserver

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.openapi.management.OpenApiManagementModule
import io.koraframework.validation.module.ValidationModule

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowPublicHttpServerModule,
    JsonModule,
    LogbackModule,
    ValidationModule,
    OpenApiManagementModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

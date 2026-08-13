package io.koraframework.guide.httpserver.advanced

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.guide.httpserver.advanced.controller.DataApiAuthConfig
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.request.HttpServerRequestHandler
import io.koraframework.http.server.common.request.HttpServerRequestHandlerImpl
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, JsonModule, LogbackModule, UndertowPublicHttpServerModule {

    fun manualDataPingHandler(authConfig: DataApiAuthConfig): HttpServerRequestHandler {
        return HttpServerRequestHandlerImpl.get("/manual/data/ping") { request ->
            val authorization = request.headers().getFirst("authorization")
            if (authConfig.value() != authorization) {
                HttpServerResponse.of(403, HttpBody.plaintext("Invalid API key"))
            } else {
                HttpServerResponse.of(200, HttpBody.plaintext("manual-data-pong"))
            }
        }
    }
}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

package ru.tinkoff.kora.guide.httpserver.advanced

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.guide.httpserver.advanced.controller.DataApiAuthConfig
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestHandler
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestHandlerImpl
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import java.util.concurrent.CompletableFuture

@KoraApp
interface Application : HoconConfigModule, JsonModule, LogbackModule, UndertowHttpServerModule {

    fun manualDataPingHandler(authConfig: DataApiAuthConfig): HttpServerRequestHandler {
        return HttpServerRequestHandlerImpl.get("/manual/data/ping") { _, request ->
            val authorization = request.headers().getFirst("authorization")
            if (authConfig.value() != authorization) {
                CompletableFuture.completedFuture(HttpServerResponse.of(403, HttpBody.plaintext("Invalid API key")))
            } else {
                CompletableFuture.completedFuture(HttpServerResponse.of(200, HttpBody.plaintext("manual-data-pong")))
            }
        }
    }
}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}


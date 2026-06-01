package ru.tinkoff.kora.guide.httpserver.advanced;

import java.util.concurrent.CompletableFuture;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.guide.httpserver.advanced.controller.DataApiAuthConfig;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestHandler;
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestHandlerImpl;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        UndertowHttpServerModule {

    default HttpServerRequestHandler manualDataPingHandler(DataApiAuthConfig authConfig) {
        return HttpServerRequestHandlerImpl.get("/manual/data/ping", (context, request) -> {
            var authorization = request.headers().getFirst("authorization");
            if (!authConfig.value().equals(authorization)) {
                return CompletableFuture.completedFuture(HttpServerResponse.of(403, HttpBody.plaintext("Invalid API key")));
            }
            return CompletableFuture.completedFuture(HttpServerResponse.of(200, HttpBody.plaintext("manual-data-pong")));
        });
    }

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

package io.koraframework.guide.httpserver.advanced;

import java.util.concurrent.CompletableFuture;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.guide.httpserver.advanced.controller.DataApiAuthConfig;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.request.HttpServerRequestHandler;
import io.koraframework.http.server.common.request.HttpServerRequestHandlerImpl;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        UndertowPublicHttpServerModule {

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

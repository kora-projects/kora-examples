package io.koraframework.guide.httpserver;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        UndertowPublicHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}


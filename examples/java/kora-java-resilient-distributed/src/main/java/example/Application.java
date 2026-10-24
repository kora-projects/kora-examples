package example;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.resilient.distributed.LettuceDistributedResilientModule;

@KoraApp
public interface Application extends HoconConfigModule, LogbackModule,
    UndertowPublicHttpServerModule, LettuceDistributedResilientModule, io.koraframework.resilient.ResilientModule, BudgetModule {
    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

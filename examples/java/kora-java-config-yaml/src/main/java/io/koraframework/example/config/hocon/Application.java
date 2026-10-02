package io.koraframework.example.config.hocon;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.yaml.YamlConfigModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        YamlConfigModule,
        LogbackModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

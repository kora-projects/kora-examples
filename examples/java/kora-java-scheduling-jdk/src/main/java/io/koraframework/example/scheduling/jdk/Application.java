package io.koraframework.example.scheduling.jdk;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.scheduling.jdk.SchedulingJdkModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        SchedulingJdkModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

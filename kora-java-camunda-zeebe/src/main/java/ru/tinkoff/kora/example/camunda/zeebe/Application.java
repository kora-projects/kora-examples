package ru.tinkoff.kora.example.camunda.zeebe;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.camunda.zeebe.worker.ZeebeWorkerModule;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.json.common.JsonCommonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.scheduling.jdk.SchedulingJdkModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JsonCommonModule,
        SchedulingJdkModule,
        ZeebeWorkerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

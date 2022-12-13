package ru.tinkoff.kora.example.validation;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.validation.common.constraint.ValidatorModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidatorModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

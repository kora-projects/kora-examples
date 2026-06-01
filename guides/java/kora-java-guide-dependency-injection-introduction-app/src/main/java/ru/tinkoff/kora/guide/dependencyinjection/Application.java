package ru.tinkoff.kora.guide.dependencyinjection;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.DefaultComponent;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Module;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends HoconConfigModule, LogbackModule, NotificationModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    default MessageFormatter messageFormatter() {
        // Overrides @DefaultComponent from NotificationModule.
        return message -> "[app] " + message;
    }
}

@Module
interface NotificationModule {

    @DefaultComponent
    default MessageFormatter defaultMessageFormatter() {
        return message -> "[default] " + message;
    }
}

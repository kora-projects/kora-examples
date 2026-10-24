package io.koraframework.guide.dependencyinjection;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.DefaultComponent;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Module;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;

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

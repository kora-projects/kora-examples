package ru.tinkoff.kora.guide.dependencyinjection;

import java.util.function.Supplier;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.guide.dependencyinjection.email.EmailModule;
import ru.tinkoff.kora.guide.dependencyinjection.messenger.MessengerModule;
import ru.tinkoff.kora.guide.dependencyinjection.sms.SmsCellularModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        EmailModule,
        SmsCellularModule,
        MessengerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    @Tag(EmailModule.EmailTag.class)
    @Override
    default Supplier<String> emailNotifierHeaderSupplier() {
        return () -> "[EMAIL OVERRIDDEN] ";
    }
}

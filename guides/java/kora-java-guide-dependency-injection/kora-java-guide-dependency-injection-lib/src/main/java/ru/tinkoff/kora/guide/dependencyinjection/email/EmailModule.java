package ru.tinkoff.kora.guide.dependencyinjection.email;

import java.util.function.Supplier;
import ru.tinkoff.kora.common.DefaultComponent;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.common.Config;
import ru.tinkoff.kora.config.common.extractor.ConfigValueExtractor;
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier;

public interface EmailModule {

    final class EmailTag {
        private EmailTag() {}
    }

    default EmailConfig config(Config config, ConfigValueExtractor<EmailConfig> extractor) {
        return extractor.extract(config.get("notifier.email"));
    }

    @Tag(EmailTag.class)
    @DefaultComponent
    default Supplier<String> emailNotifierHeaderSupplier() {
        return () -> "[EMAIL DEFAULT] ";
    }

    @Tag(EmailTag.class)
    default Notifier emailNotifier(EmailConfig emailConfig, @Tag(EmailTag.class) Supplier<String> headerSupplier) {
        return (user, message) -> System.out.println(headerSupplier.get() + emailConfig.topic() + " [USER:" + user + "]: " + message);
    }
}
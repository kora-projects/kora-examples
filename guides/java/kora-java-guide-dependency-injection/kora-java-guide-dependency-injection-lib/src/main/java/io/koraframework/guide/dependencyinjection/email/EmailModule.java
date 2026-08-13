package io.koraframework.guide.dependencyinjection.email;

import java.util.function.Supplier;
import io.koraframework.common.annotation.DefaultComponent;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.common.Config;
import io.koraframework.config.common.mapper.ConfigValueMapper;
import io.koraframework.guide.dependencyinjection.common.Notifier;

public interface EmailModule {

    final class EmailTag {
        private EmailTag() {}
    }

    default EmailConfig config(Config config, ConfigValueMapper<EmailConfig> extractor) {
        return extractor.mapOrThrow(config.get("notifier.email"));
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
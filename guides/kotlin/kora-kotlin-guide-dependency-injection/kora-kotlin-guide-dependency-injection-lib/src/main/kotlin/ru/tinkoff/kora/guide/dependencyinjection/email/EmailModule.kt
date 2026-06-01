package ru.tinkoff.kora.guide.dependencyinjection.email

import java.util.function.Supplier
import ru.tinkoff.kora.common.DefaultComponent
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.common.Config
import ru.tinkoff.kora.config.common.extractor.ConfigValueExtractor
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier

interface EmailModule {

    class EmailTag private constructor()

    fun config(config: Config, extractor: ConfigValueExtractor<EmailConfig>): EmailConfig {
        return extractor.extract(config["notifier.email"])
    }

    @Tag(EmailTag::class)
    @DefaultComponent
    fun emailNotifierHeaderSupplier(): Supplier<String> {
        return Supplier { "[EMAIL DEFAULT] " }
    }

    @Tag(EmailTag::class)
    fun emailNotifier(
        emailConfig: EmailConfig,
        @Tag(EmailTag::class) headerSupplier: Supplier<String>
    ): Notifier {
        return Notifier { user, message ->
            println("${headerSupplier.get()}${emailConfig.topic} [USER:$user]: $message")
        }
    }
}

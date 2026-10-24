package io.koraframework.guide.dependencyinjection.email

import java.util.function.Supplier
import io.koraframework.common.annotation.DefaultComponent
import io.koraframework.common.annotation.Tag
import io.koraframework.config.common.Config
import io.koraframework.config.common.mapper.ConfigValueMapper
import io.koraframework.guide.dependencyinjection.common.Notifier

interface EmailModule {

    class EmailTag private constructor()

    fun config(config: Config, extractor: ConfigValueMapper<EmailConfig>): EmailConfig {
        return extractor.mapOrThrow(config["notifier.email"])
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

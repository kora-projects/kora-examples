package io.koraframework.guide.dependencyinjection

import java.util.function.Supplier
import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.guide.dependencyinjection.email.EmailModule
import io.koraframework.guide.dependencyinjection.messenger.MessengerModule
import io.koraframework.guide.dependencyinjection.sms.SmsCellularModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    LogbackModule,
    EmailModule,
    SmsCellularModule,
    MessengerModule {

    @Tag(EmailModule.EmailTag::class)
    override fun emailNotifierHeaderSupplier(): Supplier<String> {
        return Supplier { "[EMAIL OVERRIDDEN] " }
    }
}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

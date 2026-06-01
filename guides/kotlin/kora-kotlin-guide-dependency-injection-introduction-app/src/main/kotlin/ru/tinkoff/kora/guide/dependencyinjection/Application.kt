package ru.tinkoff.kora.guide.dependencyinjection

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.DefaultComponent
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, NotificationModule {

    fun messageFormatter(): MessageFormatter {
        return MessageFormatter { message -> "[app] $message" }
    }
}

@Module
interface NotificationModule {

    @DefaultComponent
    fun defaultMessageFormatter(): MessageFormatter {
        return MessageFormatter { message -> "[default] $message" }
    }

}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

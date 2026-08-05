package io.koraframework.guide.dependencyinjection

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.DefaultComponent
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Module
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule

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

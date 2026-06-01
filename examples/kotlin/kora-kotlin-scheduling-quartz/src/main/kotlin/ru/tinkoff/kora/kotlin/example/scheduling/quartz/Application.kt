package ru.tinkoff.kora.kotlin.example.scheduling.quartz

import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.scheduling.quartz.QuartzModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, QuartzModule {
    @Tag(TriggerScheduler::class)
    fun myTrigger(): Trigger {
        return TriggerBuilder.newTrigger()
            .withIdentity("myTrigger")
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMilliseconds(50)
                    .repeatForever()
            )
            .build()
    }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

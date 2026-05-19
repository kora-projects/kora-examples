package ru.tinkoff.kora.kotlin.example.scheduling.quartz

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithCron
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithTrigger

@Component
class ConfigScheduler {
    var state = 0
        private set

    @ScheduleWithCron(config = "scheduling.jobs.quartz.cron")
    fun schedule() {
        state++
    }
}

@Component
class CronScheduler {
    var state = 0
        private set

    @ScheduleWithCron("* * * ? * * *")
    fun schedule() {
        state++
    }
}

@Component
class TriggerScheduler {
    var state = 0
        private set

    @ScheduleWithTrigger(Tag(TriggerScheduler::class))
    fun schedule() {
        state++
    }
}

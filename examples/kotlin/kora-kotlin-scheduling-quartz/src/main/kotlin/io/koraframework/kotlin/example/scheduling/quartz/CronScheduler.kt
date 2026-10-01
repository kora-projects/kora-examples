package io.koraframework.kotlin.example.scheduling.quartz

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithCron
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithTrigger

@Component
class CronScheduler {
    var state = 0
        private set

    @ScheduleQuartzWithCron("* * * ? * * *")
    fun schedule() {
        state++
    }
}


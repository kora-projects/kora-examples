package ru.tinkoff.kora.guide.dependencyinjection.activity

import ru.tinkoff.kora.application.graph.ValueOf
import ru.tinkoff.kora.common.Component

@Component
class ActivityService(
    private val activityRecorder: ValueOf<ActivityRecorder>
) {

    init {
        println("ActivityService created (ActivityRecorder not yet accessed)")
    }

    fun recordActivityByUserName(user: String) {
        println("Recording activity for: $user")
        val recorder = activityRecorder.get()
        recorder.recordUser(user)
        println("Activity recorded successfully")
    }
}

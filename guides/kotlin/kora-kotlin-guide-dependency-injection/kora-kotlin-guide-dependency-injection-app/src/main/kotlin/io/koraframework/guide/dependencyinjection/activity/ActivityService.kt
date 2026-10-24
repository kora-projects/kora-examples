package io.koraframework.guide.dependencyinjection.activity

import io.koraframework.application.graph.ValueOf
import io.koraframework.common.annotation.Component

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

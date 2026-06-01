package ru.tinkoff.kora.guide.dependencyinjection.activity

import ru.tinkoff.kora.application.graph.LifecycleWrapper
import ru.tinkoff.kora.application.graph.Wrapped
import ru.tinkoff.kora.common.Module

@Module
interface ActivityModule {

    fun activityRecorder(): Wrapped<ActivityRecorder> {
        val recorder = object : ActivityRecorder {
            private var connected = false

            override fun connect() {
                if (!connected) {
                    println("Connecting to activity recorder")
                    connected = true
                    println("Activity recorder connected")
                }
            }

            override fun disconnect() {
                if (connected) {
                    println("Disconnecting from activity recorder")
                    connected = false
                }
            }

            override fun isConnected(): Boolean = connected

            override fun recordUser(user: String) {
                if (!connected) {
                    connect()
                }
                println("Recording user activity: $user")
            }
        }

        return LifecycleWrapper(recorder, {}, ActivityRecorder::disconnect)
    }
}

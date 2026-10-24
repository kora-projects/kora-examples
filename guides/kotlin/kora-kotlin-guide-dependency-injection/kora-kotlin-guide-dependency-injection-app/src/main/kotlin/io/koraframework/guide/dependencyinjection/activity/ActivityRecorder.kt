package io.koraframework.guide.dependencyinjection.activity

interface ActivityRecorder {

    fun connect()

    fun disconnect()

    fun isConnected(): Boolean

    fun recordUser(user: String)
}

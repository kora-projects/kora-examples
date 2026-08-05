package io.koraframework.guide.dependencyinjection.common

fun interface Notifier {
    fun notify(user: String, message: String)
}

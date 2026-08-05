package io.koraframework.guide.dependencyinjection

interface Notifier {
    fun channel(): String

    fun notifyUser(message: String): String
}

package ru.tinkoff.kora.guide.dependencyinjection.messenger

fun interface Messenger {
    fun sendMessage(message: String)
}

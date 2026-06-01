package ru.tinkoff.kora.guide.dependencyinjection

fun interface MessageFormatter {
    fun format(message: String): String
}

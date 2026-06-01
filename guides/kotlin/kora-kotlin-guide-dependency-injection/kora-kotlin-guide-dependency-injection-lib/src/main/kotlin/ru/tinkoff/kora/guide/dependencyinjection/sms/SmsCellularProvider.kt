package ru.tinkoff.kora.guide.dependencyinjection.sms

fun interface SmsCellularProvider {
    fun getCode(): String
}

package io.koraframework.guide.dependencyinjection.sms

fun interface SmsCellularProvider {
    fun getCode(): String
}

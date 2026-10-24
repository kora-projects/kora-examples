package io.koraframework.guide.dependencyinjection

fun interface MessageFormatter {
    fun format(message: String): String
}

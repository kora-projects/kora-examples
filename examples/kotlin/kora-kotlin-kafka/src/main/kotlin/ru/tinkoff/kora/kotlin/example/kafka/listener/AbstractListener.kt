package ru.tinkoff.kora.kotlin.example.kafka.listener

abstract class AbstractListener<T> {
    private val events = ArrayList<T>()
    private val exceptions = ArrayList<Exception>()

    fun success(event: T) {
        events.add(event)
    }

    fun fail(exception: Exception) {
        exceptions.add(exception)
    }

    fun received(): List<T> = events

    fun failed(): List<Exception> = exceptions

    fun reset() {
        events.clear()
        exceptions.clear()
    }
}

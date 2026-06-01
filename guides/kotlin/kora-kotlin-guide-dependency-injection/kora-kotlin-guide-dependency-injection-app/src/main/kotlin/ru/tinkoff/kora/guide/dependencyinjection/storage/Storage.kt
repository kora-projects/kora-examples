package ru.tinkoff.kora.guide.dependencyinjection.storage

interface Storage<T> {
    fun save(data: T)
}

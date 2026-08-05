package io.koraframework.guide.dependencyinjection.storage

interface Storage<T> {
    fun save(data: T)
}

package ru.tinkoff.kora.guide.dependencyinjection.storage;

public interface Storage<T> {
    void save(T data);
}
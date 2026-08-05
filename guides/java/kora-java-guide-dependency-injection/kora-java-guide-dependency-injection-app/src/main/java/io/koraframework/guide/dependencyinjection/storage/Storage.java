package io.koraframework.guide.dependencyinjection.storage;

public interface Storage<T> {
    void save(T data);
}
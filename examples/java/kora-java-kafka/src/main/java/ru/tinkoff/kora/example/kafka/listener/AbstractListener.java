package ru.tinkoff.kora.example.kafka.listener;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListener<T> {

    private final List<T> events = new ArrayList<>();
    private final List<Exception> exceptions = new ArrayList<>();

    void success(T event) {
        events.add(event);
    }

    void fail(Exception exception) {
        exceptions.add(exception);
    }

    public List<T> received() {
        return events;
    }

    public List<Exception> failed() {
        return exceptions;
    }

    public void reset() {
        events.clear();
        exceptions.clear();
    }
}

package ru.tinkoff.kora.guide.dependencyinjection;

public interface AuditSink {
    void record(String channel, String message);
}
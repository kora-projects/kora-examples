package io.koraframework.guide.dependencyinjection;

public interface AuditSink {
    void record(String channel, String message);
}
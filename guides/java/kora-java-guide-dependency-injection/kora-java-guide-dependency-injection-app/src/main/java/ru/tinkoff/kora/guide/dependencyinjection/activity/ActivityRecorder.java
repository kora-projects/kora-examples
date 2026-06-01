package ru.tinkoff.kora.guide.dependencyinjection.activity;

public interface ActivityRecorder {

    void connect();

    void disconnect();

    boolean isConnected();

    void recordUser(String user);
}
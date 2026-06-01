package ru.tinkoff.kora.guide.dependencyinjection;

public interface Notifier {
    String channel();

    String notifyUser(String message);
}

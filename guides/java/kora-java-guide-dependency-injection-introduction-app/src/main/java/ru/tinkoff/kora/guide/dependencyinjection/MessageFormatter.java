package ru.tinkoff.kora.guide.dependencyinjection;

@FunctionalInterface
public interface MessageFormatter {
    String format(String message);
}

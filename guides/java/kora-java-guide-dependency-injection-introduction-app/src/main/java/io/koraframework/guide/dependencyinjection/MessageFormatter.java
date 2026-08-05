package io.koraframework.guide.dependencyinjection;

@FunctionalInterface
public interface MessageFormatter {
    String format(String message);
}

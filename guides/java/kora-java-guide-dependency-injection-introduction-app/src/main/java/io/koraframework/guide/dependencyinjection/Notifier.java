package io.koraframework.guide.dependencyinjection;

public interface Notifier {
    String channel();

    String notifyUser(String message);
}

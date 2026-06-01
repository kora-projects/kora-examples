package ru.tinkoff.kora.guide.dependencyinjection.sms;

import ru.tinkoff.kora.common.DefaultComponent;

public interface SmsCellularModule {

    @DefaultComponent
    default SmsCellularProvider smsCellularProvider() {
        return () -> "1";
    }
}
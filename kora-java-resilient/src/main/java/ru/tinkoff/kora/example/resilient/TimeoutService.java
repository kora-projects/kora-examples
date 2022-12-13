package ru.tinkoff.kora.example.resilient;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Component
public class TimeoutService {

    @Timeout("custom1")
    public String getSuccessful() {
        return "OK";
    }
}

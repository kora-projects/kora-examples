package ru.tinkoff.kora.example.resilient;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;

@Component
public class RetryService {

    @Retry("custom1")
    public String getValue(String arg) {
        return arg;
    }
}

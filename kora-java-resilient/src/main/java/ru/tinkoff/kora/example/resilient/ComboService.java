package ru.tinkoff.kora.example.resilient;

import java.util.concurrent.ThreadLocalRandom;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Root
@Component
public class ComboService {

    public static final String VALUE = "OK";
    public static final String FALLBACK = "FALLBACK";

    @Fallback(value = "my_fallback", method = "getFallback()")
    @CircuitBreaker("my_cb")
    @Retry("my_retry")
    @Timeout("my_timeout")
    public String getValue(boolean fail) {
        if (fail) {
            throw new IllegalStateException("Failed");
        }

        try {
            int emulateWork = ThreadLocalRandom.current().nextInt(100, 1000);
            Thread.sleep(emulateWork);
            return VALUE;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected String getFallback() {
        return FALLBACK;
    }
}

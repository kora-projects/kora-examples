package io.koraframework.example.resilient;

import java.util.concurrent.ThreadLocalRandom;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;
import io.koraframework.resilient.fallback.annotation.Fallback;
import io.koraframework.resilient.retry.annotation.Retryable;
import io.koraframework.resilient.timeout.annotation.Timeout;

@Root
@Component
public class ComboService {

    public static final String VALUE = "OK";
    public static final String FALLBACK = "FALLBACK";

    @Fallback(method = "getFallback()")
    @CircuitBreakable(MyCircuitBreaker.class)
    @Retryable(DefaultRetry.class)
    @Timeout(DefaultTimeouter.class)
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

package ru.tinkoff.kora.example.resilient;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback;

@Component
public class FallbackService {

    public static final String VALUE = "OK";
    public static final String FALLBACK = "FALLBACK";

    @Fallback(value = "my_fallback", method = "getFallback()")
    public String getValue(boolean fail) {
        if (fail)
            throw new IllegalStateException("Failed");

        return VALUE;
    }

    protected String getFallback() {
        return FALLBACK;
    }
}

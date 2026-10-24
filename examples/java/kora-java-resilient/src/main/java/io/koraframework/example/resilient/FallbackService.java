package io.koraframework.example.resilient;

import io.koraframework.common.annotation.Component;
import io.koraframework.resilient.fallback.annotation.Fallback;

@Component
public class FallbackService {

    public static final String VALUE = "OK";
    public static final String FALLBACK = "FALLBACK";

    @Fallback(method = "getFallback()")
    public String getValue(boolean fail) {
        if (fail)
            throw new IllegalStateException("Failed");

        return VALUE;
    }

    protected String getFallback() {
        return FALLBACK;
    }
}

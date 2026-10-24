package io.koraframework.example.resilient;

import io.koraframework.common.annotation.Component;
import io.koraframework.resilient.retry.annotation.Retryable;

@Component
public class RetryService {

    @Retryable(DefaultRetry.class)
    public String getValue(String arg) {
        return arg;
    }
}

package io.koraframework.example.resilient;

import io.koraframework.common.annotation.Component;
import io.koraframework.resilient.timeout.annotation.Timeout;

@Component
public class TimeoutService {

    @Timeout(DefaultTimeouter.class)
    public String getSuccessful() {
        return "OK";
    }
}

package io.koraframework.guide.dependencyinjection.sms;

import io.koraframework.common.annotation.DefaultComponent;

public interface SmsCellularModule {

    @DefaultComponent
    default SmsCellularProvider smsCellularProvider() {
        return () -> "1";
    }
}
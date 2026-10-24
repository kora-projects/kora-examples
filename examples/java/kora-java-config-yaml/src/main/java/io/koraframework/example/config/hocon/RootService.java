package io.koraframework.example.config.hocon;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final FooConfig fooConfig;

    public RootService(FooConfig fooConfig) {
        this.fooConfig = fooConfig;
    }
}

package ru.tinkoff.kora.example.config.hocon;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class RootService {

    private final FooConfig fooConfig;

    public RootService(FooConfig fooConfig) {
        this.fooConfig = fooConfig;
    }
}

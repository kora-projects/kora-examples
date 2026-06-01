package ru.tinkoff.kora.guide.config.hocon;

import ru.tinkoff.kora.config.common.annotation.ConfigSource;

@ConfigSource("app")
public interface AppConfig {

    String name();

    String version();

    String environment();
}


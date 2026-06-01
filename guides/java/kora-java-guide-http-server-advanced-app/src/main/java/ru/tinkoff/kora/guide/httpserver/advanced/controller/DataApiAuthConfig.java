package ru.tinkoff.kora.guide.httpserver.advanced.controller;

import ru.tinkoff.kora.config.common.annotation.ConfigSource;

@ConfigSource("auth.apiKey")
public interface DataApiAuthConfig {

    String value();
}


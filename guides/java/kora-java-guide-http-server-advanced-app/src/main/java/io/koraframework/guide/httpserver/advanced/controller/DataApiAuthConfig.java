package io.koraframework.guide.httpserver.advanced.controller;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("auth.apiKey")
public interface DataApiAuthConfig {

    String value();
}


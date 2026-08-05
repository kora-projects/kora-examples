package io.koraframework.guide.config.yaml;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("app")
public interface AppConfig {

    String name();

    String version();

    String environment();
}


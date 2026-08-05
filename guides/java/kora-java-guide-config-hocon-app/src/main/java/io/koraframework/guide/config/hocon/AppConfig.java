package io.koraframework.guide.config.hocon;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("app")
public interface AppConfig {

    String name();

    String version();

    String environment();
}


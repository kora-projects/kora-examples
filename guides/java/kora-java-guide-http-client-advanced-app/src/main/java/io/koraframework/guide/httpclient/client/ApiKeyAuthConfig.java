package io.koraframework.guide.httpclient.client;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("auth.apiKey")
public interface ApiKeyAuthConfig {

    String value();
}

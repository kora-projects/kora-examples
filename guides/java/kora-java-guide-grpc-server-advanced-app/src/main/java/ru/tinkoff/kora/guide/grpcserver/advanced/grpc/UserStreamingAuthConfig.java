package ru.tinkoff.kora.guide.grpcserver.advanced.grpc;

import ru.tinkoff.kora.config.common.annotation.ConfigSource;

@ConfigSource("auth.apiKey")
public interface UserStreamingAuthConfig {

    String value();
}

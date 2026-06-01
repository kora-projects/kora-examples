package ru.tinkoff.kora.guide.config.yaml;

import java.time.Duration;
import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor;

@ConfigValueExtractor
public interface LibConfig {

    String endpoint();

    Duration requestTimeout();
}


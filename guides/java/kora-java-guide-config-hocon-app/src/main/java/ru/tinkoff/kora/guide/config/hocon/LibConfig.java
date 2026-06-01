package ru.tinkoff.kora.guide.config.hocon;

import java.time.Duration;
import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor;

@ConfigValueExtractor
public interface LibConfig {

    String endpoint();

    Duration requestTimeout();
}


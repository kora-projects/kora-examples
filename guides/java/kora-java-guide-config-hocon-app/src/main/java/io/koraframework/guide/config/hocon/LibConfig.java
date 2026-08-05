package io.koraframework.guide.config.hocon;

import java.time.Duration;
import io.koraframework.config.common.annotation.ConfigMapper;

@ConfigMapper
public interface LibConfig {

    String endpoint();

    Duration requestTimeout();
}


package io.koraframework.guide.config.yaml;

import java.time.Duration;
import io.koraframework.config.common.annotation.ConfigMapper;

@ConfigMapper
public interface LibConfig {

    String endpoint();

    Duration requestTimeout();
}


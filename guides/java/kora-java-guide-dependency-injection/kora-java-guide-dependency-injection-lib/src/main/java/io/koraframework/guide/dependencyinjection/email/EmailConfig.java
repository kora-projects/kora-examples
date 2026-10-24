package io.koraframework.guide.dependencyinjection.email;

import io.koraframework.config.common.annotation.ConfigMapper;

@ConfigMapper
public record EmailConfig(String topic) {}

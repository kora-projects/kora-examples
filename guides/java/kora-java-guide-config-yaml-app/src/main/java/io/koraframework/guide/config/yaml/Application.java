package io.koraframework.guide.config.yaml;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.common.Config;
import io.koraframework.config.common.extractor.ConfigMapper;
import io.koraframework.config.yaml.YamlConfigModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends YamlConfigModule, LogbackModule {

    final class Lib1Tag {}

    final class Lib2Tag {}

    @Tag(Lib1Tag.class)
    default LibConfig lib1Config(Config config, ConfigMapper<LibConfig> extractor) {
        return extractor.extract(config.get("libs.lib1"));
    }

    @Tag(Lib2Tag.class)
    default LibConfig lib2Config(Config config, ConfigMapper<LibConfig> extractor) {
        return extractor.extract(config.get("libs.lib2"));
    }

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

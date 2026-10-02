package io.koraframework.guide.config.hocon;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.common.Config;
import io.koraframework.config.common.mapper.ConfigValueMapper;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends HoconConfigModule, LogbackModule {

    final class Lib1Tag {}

    final class Lib2Tag {}

    @Tag(Lib1Tag.class)
    default LibConfig lib1Config(Config config, ConfigValueMapper<LibConfig> extractor) {
        return extractor.mapOrThrow(config.get("libs.lib1"));
    }

    @Tag(Lib2Tag.class)
    default LibConfig lib2Config(Config config, ConfigValueMapper<LibConfig> extractor) {
        return extractor.mapOrThrow(config.get("libs.lib2"));
    }

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

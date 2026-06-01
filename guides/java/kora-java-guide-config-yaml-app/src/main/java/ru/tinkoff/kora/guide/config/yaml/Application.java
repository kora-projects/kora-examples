package ru.tinkoff.kora.guide.config.yaml;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.common.Config;
import ru.tinkoff.kora.config.common.extractor.ConfigValueExtractor;
import ru.tinkoff.kora.config.yaml.YamlConfigModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends YamlConfigModule, LogbackModule {

    final class Lib1Tag {}

    final class Lib2Tag {}

    @Tag(Lib1Tag.class)
    default LibConfig lib1Config(Config config, ConfigValueExtractor<LibConfig> extractor) {
        return extractor.extract(config.get("libs.lib1"));
    }

    @Tag(Lib2Tag.class)
    default LibConfig lib2Config(Config config, ConfigValueExtractor<LibConfig> extractor) {
        return extractor.extract(config.get("libs.lib2"));
    }

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

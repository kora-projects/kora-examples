package ru.tinkoff.kora.guide.config.yaml

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.common.Config
import ru.tinkoff.kora.config.common.extractor.ConfigValueExtractor
import ru.tinkoff.kora.config.yaml.YamlConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : YamlConfigModule, LogbackModule {

    class Lib1Tag private constructor()
    class Lib2Tag private constructor()

    @Tag(Lib1Tag::class)
    fun lib1Config(config: Config, extractor: ConfigValueExtractor<LibConfig>): LibConfig {
        return extractor.extract(config.get("libs.lib1"))
    }

    @Tag(Lib2Tag::class)
    fun lib2Config(config: Config, extractor: ConfigValueExtractor<LibConfig>): LibConfig {
        return extractor.extract(config.get("libs.lib2"))
    }

}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

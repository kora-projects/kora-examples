package io.koraframework.guide.config.yaml

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.config.common.Config
import io.koraframework.config.common.mapper.ConfigValueMapper
import io.koraframework.config.yaml.YamlConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : YamlConfigModule, LogbackModule {

    class Lib1Tag private constructor()
    class Lib2Tag private constructor()

    @Tag(Lib1Tag::class)
    fun lib1Config(config: Config, extractor: ConfigValueMapper<LibConfig>): LibConfig {
        return extractor.mapOrThrow(config.get("libs.lib1"))
    }

    @Tag(Lib2Tag::class)
    fun lib2Config(config: Config, extractor: ConfigValueMapper<LibConfig>): LibConfig {
        return extractor.mapOrThrow(config.get("libs.lib2"))
    }

}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

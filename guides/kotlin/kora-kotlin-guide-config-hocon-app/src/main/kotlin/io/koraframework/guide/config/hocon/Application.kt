package io.koraframework.guide.config.hocon

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.config.common.Config
import io.koraframework.config.common.extractor.ConfigMapper
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule {

    class Lib1Tag private constructor()
    class Lib2Tag private constructor()

    @Tag(Lib1Tag::class)
    fun lib1Config(config: Config, extractor: ConfigMapper<LibConfig>): LibConfig {
        return extractor.extract(config.get("libs.lib1"))
    }

    @Tag(Lib2Tag::class)
    fun lib2Config(config: Config, extractor: ConfigMapper<LibConfig>): LibConfig {
        return extractor.extract(config.get("libs.lib2"))
    }

}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

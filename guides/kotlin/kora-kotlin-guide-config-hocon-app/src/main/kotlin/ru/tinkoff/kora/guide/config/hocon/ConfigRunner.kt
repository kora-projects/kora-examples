package ru.tinkoff.kora.guide.config.hocon

import ru.tinkoff.kora.application.graph.Lifecycle
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root

@Root
@Component
class ConfigRunner(
    private val appConfig: AppConfig,
    @Tag(Application.Lib1Tag::class) private val lib1Config: LibConfig,
    @Tag(Application.Lib2Tag::class) private val lib2Config: LibConfig,
) : Lifecycle {

    fun snapshot(): Map<String, String> {
        return linkedMapOf(
            "name" to appConfig.name(),
            "version" to appConfig.version(),
            "environment" to appConfig.environment(),
            "lib1.endpoint" to lib1Config.endpoint(),
            "lib1.requestTimeout" to lib1Config.requestTimeout().toString(),
            "lib2.endpoint" to lib2Config.endpoint(),
            "lib2.requestTimeout" to lib2Config.requestTimeout().toString(),
        )
    }

    override fun init() {
        println("Config guide start")
        snapshot().forEach { (key, value) -> println("$key=$value") }
    }

    override fun release() {
        println("Application shutdown")
    }
}

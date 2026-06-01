package ru.tinkoff.kora.guide.config.yaml

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration

@KoraAppTest(Application::class)
class ConfigAppTest : KoraAppTestConfigModifier {
    @TestComponent
    lateinit var appConfig: AppConfig

    @TestComponent
    lateinit var configRunner: ConfigRunner

    @TestComponent
    @Tag(Application.Lib1Tag::class)
    lateinit var lib1Config: LibConfig

    @TestComponent
    @Tag(Application.Lib2Tag::class)
    lateinit var lib2Config: LibConfig

    override fun config(): KoraConfigModification = KoraConfigModification.ofResourceFile("application.yaml")
        .withSystemProperty("APP_VERSION", "1.0.0-test")

    @Test
    fun configIsBoundToTypedInterface() {
        assertNotNull(appConfig)
        assertEquals("Task Management App", appConfig.name())
        assertEquals("1.0.0-test", appConfig.version())
        assertEquals("development", appConfig.environment())
    }

    @Test
    fun taggedLibConfigsReuseCommonSectionWithOverride() {
        assertEquals("https://integration.local/api", lib1Config.endpoint())
        assertEquals(Duration.ofSeconds(5), lib1Config.requestTimeout())
        assertEquals("https://integration-2.local/api", lib2Config.endpoint())
        assertEquals(Duration.ofSeconds(5), lib2Config.requestTimeout())
    }

    @Test
    fun runnerExposesResolvedConfigurationSnapshot() {
        val snapshot = configRunner.snapshot()
        assertEquals("Task Management App", snapshot["name"])
        assertEquals("1.0.0-test", snapshot["version"])
        assertEquals("development", snapshot["environment"])
        assertEquals("https://integration.local/api", snapshot["lib1.endpoint"])
        assertEquals("PT5S", snapshot["lib1.requestTimeout"])
        assertEquals("https://integration-2.local/api", snapshot["lib2.endpoint"])
        assertEquals("PT5S", snapshot["lib2.requestTimeout"])
    }
}

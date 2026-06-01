package ru.tinkoff.kora.guide.config.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class ConfigAppTest implements KoraAppTestConfigModifier {

    @TestComponent
    private AppConfig appConfig;

    @TestComponent
    private ConfigRunner configRunner;

    @TestComponent
    @Tag(Application.Lib1Tag.class)
    private LibConfig lib1Config;

    @TestComponent
    @Tag(Application.Lib2Tag.class)
    private LibConfig lib2Config;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofResourceFile("application.yaml")
            .withSystemProperty("APP_VERSION", "1.0.0-test");
    }

    @Test
    void configIsBoundToTypedInterface() {
        assertNotNull(this.appConfig);
        assertEquals("Task Management App", this.appConfig.name());
        assertEquals("1.0.0-test", this.appConfig.version());
        assertEquals("development", this.appConfig.environment());
    }

    @Test
    void taggedLibConfigsReuseCommonSectionWithOverride() {
        assertNotNull(this.lib1Config);
        assertNotNull(this.lib2Config);
        assertEquals("https://integration.local/api", this.lib1Config.endpoint());
        assertEquals(Duration.ofSeconds(5), this.lib1Config.requestTimeout());
        assertEquals("https://integration-2.local/api", this.lib2Config.endpoint());
        assertEquals(Duration.ofSeconds(5), this.lib2Config.requestTimeout());
    }

    @Test
    void runnerExposesResolvedConfigurationSnapshot() {
        Map<String, String> snapshot = this.configRunner.snapshot();

        assertNotNull(snapshot);
        assertEquals("Task Management App", snapshot.get("name"));
        assertEquals("1.0.0-test", snapshot.get("version"));
        assertEquals("development", snapshot.get("environment"));
        assertEquals("https://integration.local/api", snapshot.get("lib1.endpoint"));
        assertEquals("PT5S", snapshot.get("lib1.requestTimeout"));
        assertEquals("https://integration-2.local/api", snapshot.get("lib2.endpoint"));
        assertEquals("PT5S", snapshot.get("lib2.requestTimeout"));
    }
}

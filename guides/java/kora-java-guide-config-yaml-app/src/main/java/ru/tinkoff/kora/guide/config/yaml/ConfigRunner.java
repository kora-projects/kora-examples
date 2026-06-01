package ru.tinkoff.kora.guide.config.yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.tinkoff.kora.application.graph.Lifecycle;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.common.annotation.Root;

@Root
@Component
public final class ConfigRunner implements Lifecycle {

    private final AppConfig appConfig;
    private final LibConfig lib1Config;
    private final LibConfig lib2Config;

    public ConfigRunner(
        AppConfig appConfig,
        @Tag(Application.Lib1Tag.class) LibConfig lib1Config,
        @Tag(Application.Lib2Tag.class) LibConfig lib2Config
    ) {
        this.appConfig = appConfig;
        this.lib1Config = lib1Config;
        this.lib2Config = lib2Config;
    }

    public Map<String, String> snapshot() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", this.appConfig.name());
        values.put("version", this.appConfig.version());
        values.put("environment", this.appConfig.environment());
        values.put("lib1.endpoint", this.lib1Config.endpoint());
        values.put("lib1.requestTimeout", this.lib1Config.requestTimeout().toString());
        values.put("lib2.endpoint", this.lib2Config.endpoint());
        values.put("lib2.requestTimeout", this.lib2Config.requestTimeout().toString());
        return values;
    }

    @Override
    public void init() {
        System.out.println("Config guide start");
        this.snapshot().forEach((key, value) -> System.out.println(key + "=" + value));
    }

    @Override
    public void release() {
        System.out.println("Application shutdown");
    }
}

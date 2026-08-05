package io.koraframework.example.openapi.http.client;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.example.openapi.petV3.api.ApiSecurity;
import io.koraframework.http.client.common.auth.HttpClientTokenProvider;
import io.koraframework.http.client.jdk.JdkHttpClientModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.validation.module.ValidationModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        JdkHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    // Сгенерированный ApiSecurity требует HttpClientTokenProvider под тегом каждой схемы;
    // basicAuth он собирает сам из конфига, остальные предоставляет приложение
    @Tag(ApiSecurity.bearerAuth.class)
    default HttpClientTokenProvider bearerAuthTokenProvider() {
        return request -> "bearer-token";
    }

    @Tag(ApiSecurity.oAuth.class)
    default HttpClientTokenProvider oAuthTokenProvider() {
        return request -> "oauth-token";
    }
}

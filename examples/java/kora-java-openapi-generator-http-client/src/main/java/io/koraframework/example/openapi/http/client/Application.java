package io.koraframework.example.openapi.http.client;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.example.openapi.petV3.api.ApiSecurity;
import io.koraframework.http.client.common.auth.HttpClientTokenProvider;
import io.koraframework.http.client.apache.ApacheHttpClientModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.validation.module.ValidationModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        ApacheHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    // Сгенерированный ApiSecurity требует HttpClientTokenProvider под тегом каждой схемы, даже если
    // приложение её не использует. Перехватчик перебирает схемы по порядку и берёт первую, чей
    // провайдер вернул токен, поэтому неиспользуемая схема обязана вернуть null: иначе она перебьёт
    // apiKeyAuth, и запрос уйдёт с чужим заголовком.
    @Tag(ApiSecurity.BearerAuth.class)
    default HttpClientTokenProvider bearerAuthTokenProvider() {
        return request -> null;
    }

    @Tag(ApiSecurity.OAuth.class)
    default HttpClientTokenProvider oAuthTokenProvider() {
        return request -> null;
    }
}

package io.koraframework.kotlin.example.openapi.http.client

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.http.client.common.auth.HttpClientTokenProvider
import io.koraframework.kotlin.example.openapi.petV3.api.ApiSecurity
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.jdk.JdkHttpClientModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.common.constraint.ValidatorModule

@KoraApp
// ValidationModule declares an HTTP server interceptor, so it drags http-server-common into a
// client-only application; ValidatorModule is the part this example actually needs
interface Application : HoconConfigModule, LogbackModule, ValidatorModule, JsonModule, JdkHttpClientModule {

    // Сгенерированный ApiSecurity требует HttpClientTokenProvider под тегом каждой схемы, даже если
    // приложение её не использует. Перехватчик перебирает схемы по порядку и берёт первую, чей
    // провайдер вернул токен, поэтому неиспользуемая схема обязана вернуть null: иначе она перебьёт
    // apiKeyAuth, и запрос уйдёт с чужим заголовком.
    @Tag(ApiSecurity.bearerAuth::class)
    fun bearerAuthTokenProvider(): HttpClientTokenProvider = HttpClientTokenProvider { null }

    @Tag(ApiSecurity.oAuth::class)
    fun oAuthTokenProvider(): HttpClientTokenProvider = HttpClientTokenProvider { null }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

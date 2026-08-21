package io.koraframework.kotlin.example.openapi.http.client

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.http.client.common.auth.HttpClientTokenProvider
import io.koraframework.kotlin.example.openapi.petV3.api.ApiSecurity
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.ok.OkHttpClientModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.common.constraint.ValidatorModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidatorModule, JsonModule, OkHttpClientModule {

    // Сгенерированный ApiSecurity требует HttpClientTokenProvider под тегом каждой схемы, даже если
    // приложение её не использует. Перехватчик перебирает схемы по порядку и берёт первую, чей
    // провайдер вернул токен, поэтому неиспользуемая схема обязана вернуть null: иначе она перебьёт
    // apiKeyAuth, и запрос уйдёт с чужим заголовком.
    @Tag(ApiSecurity.BearerAuth::class)
    fun bearerAuthTokenProvider(): HttpClientTokenProvider = HttpClientTokenProvider { null }

    @Tag(ApiSecurity.OAuth::class)
    fun oAuthTokenProvider(): HttpClientTokenProvider = HttpClientTokenProvider { null }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

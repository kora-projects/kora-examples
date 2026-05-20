package ru.tinkoff.kora.kotlin.example.httpserver

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kotlin.example.http.server.Application
import ru.tinkoff.kora.kotlin.example.http.server.JsonPostController

@KoraApp
interface TestApplication : Application, JdkHttpClientModule {

    @Root
    @Component
    @HttpClient(configPath = "testHttpClient")
    interface JsonHttpClient {

        @HttpRoute(method = HttpMethod.POST, path = "/json")
        @Json
        fun post(@Json body: JsonPostController.JsonRequest): JsonPostController.JsonResponse
    }
}

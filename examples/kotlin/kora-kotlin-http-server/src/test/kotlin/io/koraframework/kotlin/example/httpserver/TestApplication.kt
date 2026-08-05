package io.koraframework.kotlin.example.httpserver

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Root
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.jdk.JdkHttpClientModule
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.json.common.annotation.Json
import io.koraframework.kotlin.example.http.server.Application
import io.koraframework.kotlin.example.http.server.JsonPostController

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

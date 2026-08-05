package io.koraframework.kotlin.example.http.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute

@HttpClient("httpClient.default")
interface VoidHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    fun sync()

    // the generator rejects a suspend @HttpRoute in 2.0, so the coroutine entry point is a plain
    // default function that bridges to the blocking call
    suspend fun suspendRequest() = withContext(Dispatchers.IO) { sync() }
}

package io.koraframework.guide.httpclient.client

import io.koraframework.common.annotation.Component
import io.koraframework.http.client.common.HttpClient
import io.koraframework.http.client.common.request.HttpClientRequest
import java.nio.charset.StandardCharsets

@Component
class ManualDataHttpClient(
    private val httpClient: HttpClient,
    private val dataApiConfig: `$DataApiClient_Config`,
    private val apiKeyAuthInterceptor: ApiKeyAuthInterceptor
) {

    fun pingManualHandler(): String {
        val request = HttpClientRequest.of("GET", dataApiConfig.url() + "/manual/data/ping")
            .build()
        val response = httpClient.with(apiKeyAuthInterceptor).execute(request)
        if (response.code() != 200) {
            throw IllegalStateException("Manual HTTP call failed with status ${response.code()}")
        }
        response.body().asInputStream().use { body ->
            return String(body.readAllBytes(), StandardCharsets.UTF_8)
        }
    }
}

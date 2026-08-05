package io.koraframework.kotlin.example.http.client

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.annotation.ResponseCodeMapper
import io.koraframework.http.client.common.annotation.ResponseCodeMapper.DEFAULT
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.request.HttpClientRequestMapper
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.client.common.response.HttpClientResponseMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.body.HttpBodyOutput
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.json.common.annotation.Json
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

@HttpClient("httpClient.default")
interface FormHttpClient {
    @HttpRoute(method = HttpMethod.POST, path = "/form/encoded")
    fun formEncoded(body: FormUrlEncoded): HttpResponseEntity<String>

    @HttpRoute(method = HttpMethod.POST, path = "/form/multipart")
    fun formMultipart(body: FormMultipart): HttpResponseEntity<String>
}


package io.koraframework.kotlin.example.http.server

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Tag
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.*
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequestMapper
import io.koraframework.http.server.common.response.HttpServerResponseMapper
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size
import io.koraframework.validation.common.annotation.Validate
import java.util.concurrent.CompletionStage

@Component
@HttpController
class JsonGetController {
    @Json
    data class HelloWorldResponse(val greeting: String)

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json")
    fun get(): HelloWorldResponse = HelloWorldResponse("Hello world")

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json/entity")
    fun getEntity(): HttpResponseEntity<HelloWorldResponse> =
        HttpResponseEntity.of(201, HelloWorldResponse("Hello world"))
}


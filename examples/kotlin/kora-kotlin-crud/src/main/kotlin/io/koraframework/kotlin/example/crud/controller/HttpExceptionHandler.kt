package io.koraframework.kotlin.example.crud.controller

import io.micrometer.core.instrument.config.validate.ValidationException
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.example.crud.openapi.http.server.model.MessageTO
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.HttpServer
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.JsonWriter
import java.util.concurrent.TimeoutException

@Tag(HttpServer::class)
@Component
class HttpExceptionHandler(private val errorJsonWriter: JsonWriter<MessageTO>) : HttpServerInterceptor {

    companion object {
        val logger = LoggerFactory.getLogger(HttpExceptionHandler::class.java)!!
    }

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
        try {
            return chain.process(request)
        } catch (e: Exception) {
            if (e is HttpServerResponseException) {
                return e
            }

            val body = HttpBody.json(errorJsonWriter.toByteArray(MessageTO(e.message)))
            return when (e) {
                is ValidationException -> HttpServerResponse.of(400, body)
                is IllegalArgumentException -> HttpServerResponse.of(400, body)
                is TimeoutException -> HttpServerResponse.of(408, body)
                else -> {
                    logger.error("Request '{} {}' failed", request.method(), request.path(), e)
                    HttpServerResponse.of(500, body)
                }
            }
        }
    }
}

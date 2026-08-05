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

@InterceptWith(ErrorHandlerController.ErrorHandlerInterceptor::class)
@Component
@HttpController
class ErrorHandlerController {
    @Json
    data class Error(val id: String, val code: String, val message: String?)

    @Component
    class ErrorHandlerInterceptor(private val errorJsonWriter: JsonWriter<Error>) : HttpServerInterceptor {
        override fun intercept(
            context: Context,
            request: HttpServerRequest,
            chain: HttpServerInterceptor.InterceptChain
        ): CompletionStage<HttpServerResponse> {
            return chain.process(context, request).exceptionally { e ->
                if (e is HttpServerResponseException) {
                    return@exceptionally e
                }

                val code: Int
                val error: Error
                if (e is IllegalStateException) {
                    code = 400
                    error = Error("1", "BAD_REQUEST", e.message)
                } else {
                    code = 500
                    error = Error("1", "INTERNAL_ERROR", e.message)
                }
                HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)))
            }
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/intercepted/error/{id}")
    fun get(@Path id: Int): HttpServerResponse {
        if (id < 100) {
            throw IllegalStateException("ID can't be less 100")
        }
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
    }
}


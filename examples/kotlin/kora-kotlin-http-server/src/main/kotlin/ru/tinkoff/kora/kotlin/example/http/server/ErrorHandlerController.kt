package ru.tinkoff.kora.kotlin.example.http.server

import jakarta.annotation.Nullable
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.*
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.*
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestMapper
import ru.tinkoff.kora.http.server.common.handler.HttpServerResponseMapper
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.Pattern
import ru.tinkoff.kora.validation.common.annotation.Size
import ru.tinkoff.kora.validation.common.annotation.Validate
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


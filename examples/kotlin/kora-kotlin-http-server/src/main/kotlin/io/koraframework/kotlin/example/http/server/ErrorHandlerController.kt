package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.InterceptWith
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json

@InterceptWith(ErrorHandlerController.ErrorHandlerInterceptor::class)
@Component
@HttpController
class ErrorHandlerController {
    @Json
    data class Error(val id: String, val code: String, val message: String?)

    @Component
    class ErrorHandlerInterceptor(private val errorJsonWriter: JsonWriter<Error>) : HttpServerInterceptor {
        override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
            try {
                return chain.process(request)
            } catch (e: Exception) {
                // HttpServerResponseException is itself a response, so it is returned as the client already sees it
                if (e is HttpServerResponseException) {
                    return e
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
                return HttpServerResponse.of(code, HttpBody.json(errorJsonWriter.toByteArray(error)))
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

package ru.tinkoff.kora.kotlin.example.http.server

import jakarta.annotation.Nullable
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

@Component
@HttpController
class GetParametersController {
    @HttpRoute(method = HttpMethod.GET, path = "/parameters/{path}")
    fun get(
        @Path path: String,
        @Nullable @Query query: String?,
        @Nullable @Query("Queries") queries: List<String>?,
        @Nullable @Header header: String?,
        @Nullable @Header("Headers") headers: List<String>?
    ): HttpServerResponse {
        val body = "Path: $path, Query: $query, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpHeaders.of("headerName", "headerValue"), HttpBody.plaintext(body))
    }
}

@Component
@HttpController
class GetRequestController {
    @HttpRoute(method = HttpMethod.GET, path = "/request")
    fun get(request: HttpServerRequest): HttpServerResponse {
        val queries = request.queryParams()["Queries"]
        val queryValue = request.queryParams()["query"]?.stream()?.findFirst()?.orElse(null)
        val header = request.headers().getFirst("header")
        val headers = request.headers().getAll("Headers")
        val body = "Path: ${request.path()}, Query: $queryValue, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpBody.plaintext(body))
    }
}

@InterceptWith(InterceptedController.ControllerInterceptor::class)
@Component
@HttpController
class InterceptedController {
    class ControllerInterceptor : HttpServerInterceptor {
        override fun intercept(ctx: Context, request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain) =
            chain.process(ctx, request)
    }

    class MethodInterceptor : HttpServerInterceptor {
        override fun intercept(ctx: Context, request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain) =
            chain.process(ctx, request)
    }

    @Tag(HttpServerModule::class)
    @Component
    class ServerInterceptor : HttpServerInterceptor {
        override fun intercept(ctx: Context, request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain) =
            chain.process(ctx, request)
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}

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

@Component
@HttpController
class JsonPostController {
    @Json
    data class JsonRequest(val id: String)

    @Json
    data class JsonResponse(val name: String, val value: Int)

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    fun post(@Json request: JsonRequest): JsonResponse = JsonResponse("Ivan", 100)
}

@Component
@HttpController
class MapperRequestController {
    data class UserContext(val userId: String?, val traceId: String?)

    class UserContextRequestMapper : HttpServerRequestMapper<UserContext> {
        override fun apply(request: HttpServerRequest): UserContext =
            UserContext(request.headers().getFirst("x-user-id"), request.headers().getFirst("x-trace-id"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/request")
    @Mapping(UserContextRequestMapper::class)
    fun get(@Mapping(UserContextRequestMapper::class) context: UserContext): HttpServerResponse =
        HttpServerResponse.of(200, HttpBody.plaintext("${context.userId}:${context.traceId}"))
}

@Component
@HttpController
class MapperResponseController {
    data class HelloWorldResponse(val greeting: String, val name: String)

    class HelloWorldResponseMapper : HttpServerResponseMapper<HelloWorldResponse> {
        override fun apply(ctx: Context, request: HttpServerRequest, result: HelloWorldResponse): HttpServerResponse =
            HttpServerResponse.of(200, HttpBody.plaintext("${result.greeting} - ${result.name}"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/response/{name}")
    @Mapping(HelloWorldResponseMapper::class)
    fun get(@Path name: String): HelloWorldResponse = HelloWorldResponse("Hello World", name)
}

@Component
@HttpController
class MultipartController {
    @HttpRoute(method = HttpMethod.POST, path = "/multipart")
    fun post(multipart: FormMultipart): HttpServerResponse {
        val partAsString = multipart.parts().map { it.name() }.sorted().joinToString(",")
        return HttpServerResponse.of(200, HttpBody.plaintext(partAsString))
    }
}

@Component
@HttpController
class SuspendController {
    @HttpRoute(method = HttpMethod.GET, path = "/suspend")
    suspend fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}

@Component
@HttpController
class SyncController {
    @HttpRoute(method = HttpMethod.GET, path = "/sync")
    fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}

@Component
@HttpController
open class ValidateParametersController {
    @Validate
    @HttpRoute(method = HttpMethod.GET, path = "/validate/{path}")
    open fun get(
        @Path path: String,
        @Pattern("q.+") @Query query: String,
        @Size(min = 2, max = Int.MAX_VALUE) @Query("Queries") queries: List<String>,
        @Pattern("h.+") @Header header: String,
        @Size(min = 2, max = Int.MAX_VALUE) @Header("Headers") headers: List<String>
    ): HttpServerResponse {
        val body = "Path: $path, Query: $query, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpHeaders.of("headerName", "headerValue"), HttpBody.plaintext(body))
    }
}

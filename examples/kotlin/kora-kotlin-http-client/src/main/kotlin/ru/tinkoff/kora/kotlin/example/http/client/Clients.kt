package ru.tinkoff.kora.kotlin.example.http.client

import jakarta.annotation.Nullable
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.http.client.common.HttpClientDecoderException
import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper.DEFAULT
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest
import ru.tinkoff.kora.http.client.common.request.HttpClientRequestMapper
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse
import ru.tinkoff.kora.http.client.common.response.HttpClientResponseMapper
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.Header
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.InterceptWith
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.body.HttpBodyOutput
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.json.common.annotation.Json
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

@HttpClient(configPath = "httpClient.default")
interface FormHttpClient {
    @HttpRoute(method = HttpMethod.POST, path = "/form/encoded")
    fun formEncoded(body: FormUrlEncoded): HttpResponseEntity<String>

    @HttpRoute(method = HttpMethod.POST, path = "/form/multipart")
    fun formMultipart(body: FormMultipart): HttpResponseEntity<String>
}

@InterceptWith(InterceptedHttpClient.ClientInterceptor::class)
@HttpClient(configPath = "httpClient.default")
interface InterceptedHttpClient {
    class ClientInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(ClientInterceptor::class.java)

        override fun processRequest(
            ctx: Context,
            chain: HttpClientInterceptor.InterceptChain,
            request: HttpClientRequest
        ): CompletionStage<HttpClientResponse> {
            logger.info("Client Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    class MethodInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(MethodInterceptor::class.java)

        override fun processRequest(
            ctx: Context,
            chain: HttpClientInterceptor.InterceptChain,
            request: HttpClientRequest
        ): CompletionStage<HttpClientResponse> {
            logger.info("Method Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpResponseEntity<String>
}

@HttpClient(configPath = "httpClient.default")
interface JsonHttpClient {
    @Json
    data class JsonRequest(val id: String)

    @Json
    data class JsonResponse(val name: String, val value: Int)

    @HttpRoute(method = HttpMethod.POST, path = "/json")
    @Json
    fun post(@Json body: JsonRequest): JsonResponse
}

@HttpClient(configPath = "httpClient.default")
interface MapperRequestHttpClient {
    data class UserBody(val id: String)

    class UserRequestMapper : HttpClientRequestMapper<UserBody> {
        override fun apply(ctx: Context, value: UserBody): HttpBodyOutput = HttpBody.plaintext(value.id)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/mapping_request")
    fun post(@Mapping(UserRequestMapper::class) request: UserBody): HttpResponseEntity<String>
}

@HttpClient(configPath = "httpClient.default")
interface MapperResponseHttpClient {
    class ResponseSuccessMapper : HttpClientResponseMapper<UserResponse> {
        override fun apply(response: HttpClientResponse): UserResponse {
            response.body().asInputStream().use {
                val message = String(it.readAllBytes(), StandardCharsets.UTF_8)
                return UserResponse(UserResponse.Payload(message), null)
            }
        }
    }

    class ResponseErrorMapper : HttpClientResponseMapper<UserResponse> {
        override fun apply(response: HttpClientResponse): UserResponse {
            response.body().asInputStream().use {
                val message = String(it.readAllBytes(), StandardCharsets.UTF_8)
                return UserResponse(null, UserResponse.Error(response.code(), message))
            }
        }
    }

    data class UserResponse(val payload: Payload?, val error: Error?) {
        data class Error(val code: Int, val message: String)
        data class Payload(val message: String)
    }

    @ResponseCodeMapper(code = DEFAULT, mapper = ResponseErrorMapper::class)
    @ResponseCodeMapper(code = 200, mapper = ResponseSuccessMapper::class)
    @HttpRoute(method = HttpMethod.GET, path = "/mapping_by_code/{code}")
    fun get(@Path code: String): UserResponse
}

@HttpClient(configPath = "httpClient.default")
interface ParametersHttpClient {
    @HttpRoute(method = HttpMethod.POST, path = "/parameters/{path}")
    fun post(
        @Path path: String,
        @Nullable @Query query: String?,
        @Nullable @Query("queries") queries: List<String>?,
        @Nullable @Header header: String?,
        @Nullable @Header("headers") headers: List<String>?
    ): HttpResponseEntity<String>
}

@HttpClient(configPath = "httpClient.default")
interface ReactorHttpClient {
    @HttpRoute(method = HttpMethod.GET, path = "/reactor/{path}")
    fun get(@Path path: String, @Nullable @Query query: String?, @Nullable @Header header: String?): HttpResponseEntity<ByteArray>
}

@HttpClient(configPath = "httpClient.default")
interface VoidHttpClient {
    @HttpRoute(method = HttpMethod.POST, path = "/void")
    fun sync()

    @HttpRoute(method = HttpMethod.POST, path = "/void")
    fun reactor()
}

@Root
@Component
class RootService(
    private val interceptedHttpClient: InterceptedHttpClient,
    private val jsonHttpClient: JsonHttpClient,
    private val mapperRequestHttpClient: MapperRequestHttpClient,
    private val mapperResponseHttpClient: MapperResponseHttpClient,
    private val parametersHttpClient: ParametersHttpClient,
    private val reactorHttpClient: ReactorHttpClient,
    private val voidHttpClient: VoidHttpClient,
    private val formHttpClient: FormHttpClient
)

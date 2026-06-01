package ru.tinkoff.kora.guide.httpclient.client

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.http.client.common.HttpClientDecoderException
import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest
import ru.tinkoff.kora.http.client.common.request.HttpClientRequestMapper
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse
import ru.tinkoff.kora.http.client.common.response.HttpClientResponseMapper
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.InterceptWith
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.body.HttpBodyOutput
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.annotation.Json
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

@InterceptWith(ApiKeyAuthInterceptor::class)
@HttpClient(configPath = "httpClient.dataApi")
interface DataApiClient {

    @HttpRoute(method = HttpMethod.POST, path = "/data/form")
    fun processForm(body: FormUrlEncoded): String

    @HttpRoute(method = HttpMethod.POST, path = "/data/upload")
    @Json
    fun processUpload(body: FormMultipart): UploadResponse

    @HttpRoute(method = HttpMethod.POST, path = "/data/mapping-request")
    fun processMappedRequest(@Mapping(GreetingRequestMapper::class) body: PlainTextGreetingBody): String

    @InterceptWith(MethodLoggingInterceptor::class)
    @ResponseCodeMapper(code = ResponseCodeMapper.DEFAULT, mapper = MappedResponseErrorMapper::class)
    @ResponseCodeMapper(code = 200, mapper = MappedResponseSuccessMapper::class)
    @HttpRoute(method = HttpMethod.GET, path = "/data/mapping-by-code/{code}")
    fun getMappedByCode(@Path code: Int): MappedResponse

    fun sampleUpload(): UploadResponse {
        return processUpload(
            FormMultipart(
                listOf(
                    FormMultipart.data("field1", "some data content"),
                    FormMultipart.file(
                        "field2",
                        "example1.txt",
                        "text/plain",
                        "some file content".toByteArray(StandardCharsets.UTF_8)
                    )
                )
            )
        )
    }

    @Json
    data class UploadResponse(val fileCount: Int, val fileNames: List<String>)

    data class PlainTextGreetingBody(val name: String)

    class GreetingRequestMapper : HttpClientRequestMapper<PlainTextGreetingBody> {
        override fun apply(ctx: Context, value: PlainTextGreetingBody): HttpBodyOutput {
            return HttpBody.plaintext("Hello ${value.name}")
        }
    }

    sealed interface MappedResponse {
        @Json
        data class Payload(val message: String) : MappedResponse

        @Json
        data class Error(val code: Int, val message: String) : MappedResponse

        @Json
        data class ErrorPayload(val message: String)
    }

    class MappedResponseSuccessMapper(
        private val jsonReader: JsonReader<MappedResponse.Payload>
    ) : HttpClientResponseMapper<MappedResponse> {
        @Throws(IOException::class, HttpClientDecoderException::class)
        override fun apply(response: HttpClientResponse): MappedResponse {
            response.body().asInputStream().use { input ->
                return jsonReader.read(input.readAllBytes())
            }
        }
    }

    class MappedResponseErrorMapper(
        private val jsonReader: JsonReader<MappedResponse.ErrorPayload>
    ) : HttpClientResponseMapper<MappedResponse> {
        @Throws(IOException::class, HttpClientDecoderException::class)
        override fun apply(response: HttpClientResponse): MappedResponse {
            response.body().asInputStream().use { input ->
                val payload = jsonReader.read(input.readAllBytes())
                return MappedResponse.Error(response.code(), payload.message)
            }
        }
    }

    class MethodLoggingInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(MethodLoggingInterceptor::class.java)

        override fun processRequest(
            ctx: Context,
            chain: HttpClientInterceptor.InterceptChain,
            request: HttpClientRequest
        ): CompletionStage<HttpClientResponse> {
            logger.info("Advanced HTTP client interceptor invoked")
            return chain.process(ctx, request)
        }
    }
}

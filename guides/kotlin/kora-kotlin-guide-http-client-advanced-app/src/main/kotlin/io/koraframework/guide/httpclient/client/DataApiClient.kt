package io.koraframework.guide.httpclient.client

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.http.client.common.exception.HttpClientDecoderException
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.annotation.ResponseCodeMapper
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.request.HttpClientRequestMapper
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.client.common.response.HttpClientResponseMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.InterceptWith
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.body.HttpBodyOutput
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.annotation.Json
import java.io.IOException
import java.nio.charset.StandardCharsets

@InterceptWith(ApiKeyAuthInterceptor::class)
@HttpClient("httpClient.dataApi")
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

    @Component
    class GreetingRequestMapper : HttpClientRequestMapper<PlainTextGreetingBody> {
        override fun apply(value: PlainTextGreetingBody): HttpBodyOutput {
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

    @Component
    class MappedResponseSuccessMapper(
        private val jsonReader: JsonReader<MappedResponse.Payload>
    ) : HttpClientResponseMapper<MappedResponse> {
        @Throws(IOException::class, HttpClientDecoderException::class)
        override fun apply(response: HttpClientResponse): MappedResponse {
            response.body().asInputStream().use { input ->
                return requireNotNull(jsonReader.read(input.readAllBytes())) { "Empty success payload" }
            }
        }
    }

    @Component
    class MappedResponseErrorMapper(
        private val jsonReader: JsonReader<MappedResponse.ErrorPayload>
    ) : HttpClientResponseMapper<MappedResponse> {
        @Throws(IOException::class, HttpClientDecoderException::class)
        override fun apply(response: HttpClientResponse): MappedResponse {
            response.body().asInputStream().use { input ->
                val payload = requireNotNull(jsonReader.read(input.readAllBytes())) { "Empty error payload" }
                return MappedResponse.Error(response.code(), payload.message)
            }
        }
    }

    @Component
    class MethodLoggingInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(MethodLoggingInterceptor::class.java)

        override fun processRequest(chain: HttpClientInterceptor.InterceptChain, request: HttpClientRequest): HttpClientResponse {
            logger.info("Advanced HTTP client interceptor invoked")
            return chain.process(request)
        }
    }
}

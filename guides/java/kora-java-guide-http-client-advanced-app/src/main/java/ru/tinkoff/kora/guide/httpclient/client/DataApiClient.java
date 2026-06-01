package ru.tinkoff.kora.guide.httpclient.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.guide.httpclient.client.DataApiClient.MappedResponse.Error;
import ru.tinkoff.kora.guide.httpclient.client.DataApiClient.MappedResponse.Payload;
import ru.tinkoff.kora.http.client.common.HttpClientDecoderException;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper;
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor;
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest;
import ru.tinkoff.kora.http.client.common.request.HttpClientRequestMapper;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponseMapper;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.InterceptWith;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.body.HttpBodyOutput;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.http.common.form.FormUrlEncoded;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.annotation.Json;

import static ru.tinkoff.kora.guide.httpclient.client.DataApiClient.MappedResponse.*;

@InterceptWith(ApiKeyAuthInterceptor.class)
@HttpClient(configPath = "httpClient.dataApi")
public interface DataApiClient {

    @HttpRoute(method = HttpMethod.POST, path = "/data/form")
    String processForm(FormUrlEncoded body);

    @HttpRoute(method = HttpMethod.POST, path = "/data/upload")
    @Json
    UploadResponse processUpload(FormMultipart body);

    @HttpRoute(method = HttpMethod.POST, path = "/data/mapping-request")
    String processMappedRequest(@Mapping(GreetingRequestMapper.class) PlainTextGreetingBody body);

    @InterceptWith(MethodLoggingInterceptor.class)
    @ResponseCodeMapper(code = ResponseCodeMapper.DEFAULT, mapper = MappedResponseErrorMapper.class)
    @ResponseCodeMapper(code = 200, mapper = MappedResponseSuccessMapper.class)
    @HttpRoute(method = HttpMethod.GET, path = "/data/mapping-by-code/{code}")
    MappedResponse getMappedByCode(@Path int code);

    default UploadResponse sampleUpload() {
        return this.processUpload(new FormMultipart(List.of(
                FormMultipart.data("field1", "some data content"),
                FormMultipart.file("field2", "example1.txt", "text/plain", "some file content".getBytes(StandardCharsets.UTF_8)))));
    }

    @Json
    record UploadResponse(int fileCount, List<String> fileNames) {}

    record PlainTextGreetingBody(String name) {}

    final class GreetingRequestMapper implements HttpClientRequestMapper<PlainTextGreetingBody> {

        @Override
        public HttpBodyOutput apply(Context ctx, PlainTextGreetingBody value) {
            return HttpBody.plaintext("Hello " + value.name());
        }
    }

    sealed interface MappedResponse permits Payload, Error {

        @Json
        record Payload(String message) implements MappedResponse {}

        @Json
        record Error(int code, String message) implements MappedResponse {}

        @Json
        record ErrorPayload(String message) {}
    }

    final class MappedResponseSuccessMapper implements HttpClientResponseMapper<MappedResponse> {

        private final JsonReader<Payload> jsonReader;

        public MappedResponseSuccessMapper(JsonReader<Payload> jsonReader) {
            this.jsonReader = jsonReader;
        }

        @Override
        public MappedResponse apply(HttpClientResponse response) throws IOException, HttpClientDecoderException {
            try (var is = response.body().asInputStream()) {
                return this.jsonReader.read(is.readAllBytes());
            }
        }
    }

    final class MappedResponseErrorMapper implements HttpClientResponseMapper<MappedResponse> {

        private final JsonReader<ErrorPayload> jsonReader;

        public MappedResponseErrorMapper(JsonReader<ErrorPayload> jsonReader) {
            this.jsonReader = jsonReader;
        }

        @Override
        public MappedResponse apply(HttpClientResponse response) throws IOException, HttpClientDecoderException {
            try (var is = response.body().asInputStream()) {
                var payload = this.jsonReader.read(is.readAllBytes());
                return new Error(response.code(), payload.message());
            }
        }
    }

    final class MethodLoggingInterceptor implements HttpClientInterceptor {

        private static final Logger logger = LoggerFactory.getLogger(MethodLoggingInterceptor.class);

        @Override
        public CompletionStage<HttpClientResponse> processRequest(Context ctx, InterceptChain chain, HttpClientRequest request)
            throws Exception {
            logger.info("Advanced HTTP client interceptor invoked");
            return chain.process(ctx, request);
        }
    }
}

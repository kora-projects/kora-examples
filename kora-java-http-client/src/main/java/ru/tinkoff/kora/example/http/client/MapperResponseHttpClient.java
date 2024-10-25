package ru.tinkoff.kora.example.http.client;

import ru.tinkoff.kora.http.client.common.HttpClientDecoderException;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponseMapper;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper.DEFAULT;

@HttpClient(configPath = "httpClient.default")
public interface MapperResponseHttpClient {

    final class ResponseSuccessMapper implements HttpClientResponseMapper<UserResponse> {

        @Override
        public UserResponse apply(HttpClientResponse response) throws IOException, HttpClientDecoderException {
            try (var is = response.body().asInputStream()) {
                final byte[] bytes = is.readAllBytes();
                var message = new String(bytes, StandardCharsets.UTF_8);
                return new UserResponse(new UserResponse.Payload(message), null);
            }
        }
    }

    final class ResponseErrorMapper implements HttpClientResponseMapper<UserResponse> {

        @Override
        public UserResponse apply(HttpClientResponse response) throws IOException, HttpClientDecoderException {
            try (var is = response.body().asInputStream()) {
                final byte[] bytes = is.readAllBytes();
                var message = new String(bytes, StandardCharsets.UTF_8);
                return new UserResponse(null, new UserResponse.Error(response.code(), message));
            }
        }
    }

    record UserResponse(UserResponse.Payload payload, UserResponse.Error error) {

        public record Error(int code, String message) {}

        public record Payload(String message) {}
    }

    @ResponseCodeMapper(code = DEFAULT, mapper = ResponseErrorMapper.class)
    @ResponseCodeMapper(code = 200, mapper = ResponseSuccessMapper.class)
    @HttpRoute(method = HttpMethod.GET, path = "/mapping_by_code/{code}")
    UserResponse get(@Path String code);
}

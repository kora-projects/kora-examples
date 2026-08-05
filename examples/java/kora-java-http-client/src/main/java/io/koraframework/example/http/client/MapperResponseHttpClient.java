package io.koraframework.example.http.client;

import static io.koraframework.http.client.common.annotation.ResponseCodeMapper.DEFAULT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import io.koraframework.http.client.common.exception.HttpClientDecoderException;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.client.common.annotation.ResponseCodeMapper;
import io.koraframework.http.client.common.response.HttpClientResponse;
import io.koraframework.http.client.common.response.HttpClientResponseMapper;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;

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

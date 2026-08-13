package io.koraframework.guide.httpclient.client;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.guide.httpclient.dto.UserRequest;
import io.koraframework.guide.httpclient.dto.UserResponse;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import java.io.IOException;
import io.koraframework.common.annotation.Component;
import io.koraframework.http.client.common.response.HttpClientResponse;
import io.koraframework.http.client.common.response.HttpClientResponseMapper;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.Cookie;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.json.common.annotation.Json;

@HttpClient("httpClient.userApi")
public interface UserApiClient {

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    HttpResponseEntity<UserResponse> createUser(
            @Json UserRequest request,
            @Nullable @Header("X-Request-ID") String requestId,
            @Nullable @Header("User-Agent") String userAgent,
            @Nullable @Cookie("sessionId") String sessionId);

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    UserResponse getUser(@Path String userId);

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    List<UserResponse> getUsers(
            @Nullable @Query("page") Integer page,
            @Nullable @Query("size") Integer size,
            @Nullable @Query("sort") String sort);

    /**
     * Kora 2.0 ships response mappers for {@code String} and {@code byte[]} only, so a body-less
     * response that still needs its status code has to say how {@code Void} is produced. The
     * framework wraps it into {@code HttpResponseEntity<Void>} through its own template factory,
     * which is why the component is declared but never referenced with {@code @Mapping}.
     */
    @Component
    final class VoidResponseMapper implements HttpClientResponseMapper<Void> {

        @Override
        public Void apply(HttpClientResponse response) throws IOException {
            try (var body = response.body()) {
                body.asInputStream().readAllBytes();
            }
            return null;
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    HttpResponseEntity<Void> deleteUser(@Path String userId);
}

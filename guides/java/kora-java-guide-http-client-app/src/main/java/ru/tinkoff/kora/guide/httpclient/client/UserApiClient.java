package ru.tinkoff.kora.guide.httpclient.client;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.guide.httpclient.dto.UserRequest;
import ru.tinkoff.kora.guide.httpclient.dto.UserResponse;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.Cookie;
import ru.tinkoff.kora.http.common.annotation.Header;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.annotation.Query;
import ru.tinkoff.kora.json.common.annotation.Json;

@HttpClient(configPath = "httpClient.userApi")
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

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    HttpResponseEntity<Void> deleteUser(@Path String userId);
}

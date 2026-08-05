package io.koraframework.guide.httpclient.client;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.guide.httpclient.dto.UserRequest;
import io.koraframework.guide.httpclient.dto.UserResponse;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.Cookie;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.json.common.annotation.Json;

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

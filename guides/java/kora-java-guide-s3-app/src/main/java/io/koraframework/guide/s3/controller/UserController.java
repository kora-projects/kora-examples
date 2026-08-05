package io.koraframework.guide.s3.controller;

import org.jspecify.annotations.Nullable;
import java.time.Instant;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.guide.s3.dto.UserRequest;
import io.koraframework.guide.s3.dto.UserResponse;
import io.koraframework.guide.s3.http.LoggingInterceptor;
import io.koraframework.guide.s3.service.UserService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.Cookie;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.InterceptWith;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.header.HttpHeaders;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.http.server.common.request.HttpServerRequestMapper;
import io.koraframework.json.common.annotation.Json;

@Component
@HttpController
@InterceptWith(LoggingInterceptor.class)
public final class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record RequestContext(String requestId, String userAgent, String sessionId) {}

    public static final class RequestContextMapper implements HttpServerRequestMapper<RequestContext> {

        @Override
        public RequestContext apply(HttpServerRequest request) {
            String sessionId = request.cookies().stream()
                    .filter(cookie -> "sessionId".equals(cookie.name()))
                    .map(cookie -> cookie.value())
                    .findFirst()
                    .orElse(null);

            return new RequestContext(
                    request.headers().getFirst("X-Request-ID"),
                    request.headers().getFirst("User-Agent"),
                    sessionId);
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    public UserResponse getUser(@Path String userId) {
        return userService.getUser(userId)
                .orElseThrow(() -> HttpServerResponseException.of(404, "User not found"));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    public List<UserResponse> getUsers(
            @Nullable @Query("page") Integer page,
            @Nullable @Query("size") Integer size,
            @Nullable @Query("sort") String sort) {
        int pageNum = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;
        String sortBy = sort == null ? "name" : sort;
        return userService.getUsers(pageNum, pageSize, sortBy);
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    public HttpResponseEntity<UserResponse> createUser(
            @Json UserRequest request,
            @Nullable @Header("X-Request-ID") String requestId,
            @Nullable @Header("User-Agent") String userAgent,
            @Nullable @Cookie("sessionId") String sessionId,
            @Mapping(RequestContextMapper.class) RequestContext context) {
        System.out.printf(
                "Creating user with request ID: %s, user agent: %s, session ID: %s%n",
                context.requestId(), context.userAgent(), context.sessionId());

        UserResponse user = userService.createUser(request);
        return HttpResponseEntity.of(201, HttpHeaders.of(), user);
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    public HttpResponseEntity<UserResponse> updateUser(@Path String userId, @Json UserRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated);
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    public HttpServerResponse deleteUser(@Path String userId) {
        userService.deleteUser(userId);
        return HttpServerResponse.of(204, HttpBody.empty());
    }
}



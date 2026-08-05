package io.koraframework.example.http.server;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.http.server.common.request.HttpServerRequestMapper;

@Component
@HttpController
public final class MapperRequestController {

    public record UserContext(String userId, String traceId) {}

    public static final class UserContextRequestMapper implements HttpServerRequestMapper<UserContext> {

        @Override
        public UserContext apply(HttpServerRequest request) {
            return new UserContext(request.headers().getFirst("x-user-id"), request.headers().getFirst("x-trace-id"));
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/request")
    @Mapping(UserContextRequestMapper.class)
    public HttpServerResponse get(@Mapping(UserContextRequestMapper.class) UserContext context) {
        return HttpServerResponse.of(200, HttpBody.plaintext(context.userId() + ":" + context.traceId()));
    }
}

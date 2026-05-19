package ru.tinkoff.kora.example.http.server;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestMapper;

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

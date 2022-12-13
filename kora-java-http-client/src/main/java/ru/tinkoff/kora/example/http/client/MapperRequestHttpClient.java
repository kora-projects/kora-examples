package ru.tinkoff.kora.example.http.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Mapping;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.client.common.request.HttpClientRequestMapper;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.body.HttpBodyOutput;

@Component
@HttpClient(configPath = "httpClient.default")
public interface MapperRequestHttpClient {

    record UserBody(String id) {}

    final class UserRequestMapper implements HttpClientRequestMapper<UserBody> {

        @Override
        public HttpBodyOutput apply(Context ctx, UserBody value) {
            return HttpBody.plaintext(value.id());
        }
    }

    @HttpRoute(method = HttpMethod.POST, path = "/mapping_request")
    HttpResponseEntity<String> post(@Mapping(UserRequestMapper.class) UserBody request);
}

package io.koraframework.example.http.client;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Mapping;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.client.common.request.HttpClientRequestMapper;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.body.HttpBodyOutput;

@HttpClient("httpClient.default")
public interface MapperRequestHttpClient {

    record UserBody(String id) {}

    @Component
    final class UserRequestMapper implements HttpClientRequestMapper<UserBody> {

        @Override
        public HttpBodyOutput apply(UserBody value) {
            return HttpBody.plaintext(value.id());
        }
    }

    @HttpRoute(method = HttpMethod.POST, path = "/mapping_request")
    HttpResponseEntity<String> post(@Mapping(UserRequestMapper.class) UserBody request);
}

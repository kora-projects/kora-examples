package ru.tinkoff.kora.example.http.client;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.Header;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.annotation.Query;

@Component
@HttpClient(configPath = "httpClient.default")
public interface ParametersHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/parameters/{path}")
    HttpResponseEntity<String> post(@Path String path,
                                    @Nullable @Query String query,
                                    @Nullable @Query("queries") List<String> queries,
                                    @Nullable @Header String header,
                                    @Nullable @Header("headers") List<String> headers);
}

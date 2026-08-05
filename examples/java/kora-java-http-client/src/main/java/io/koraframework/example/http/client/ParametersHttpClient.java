package io.koraframework.example.http.client;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.Header;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;

@HttpClient(configPath = "httpClient.default")
public interface ParametersHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/parameters/{path}")
    HttpResponseEntity<String> post(@Path String path,
                                    @Nullable @Query String query,
                                    @Nullable @Query("queries") List<String> queries,
                                    @Nullable @Header String header,
                                    @Nullable @Header("headers") List<String> headers);
}

package io.koraframework.example.http.client;

import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.form.FormMultipart;
import io.koraframework.http.common.form.FormUrlEncoded;

@HttpClient(configPath = "httpClient.default")
public interface FormHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/form/encoded")
    HttpResponseEntity<String> formEncoded(FormUrlEncoded body);

    @HttpRoute(method = HttpMethod.POST, path = "/form/multipart")
    HttpResponseEntity<String> formMultipart(FormMultipart body);
}

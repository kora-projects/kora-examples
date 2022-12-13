package ru.tinkoff.kora.example.http.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.http.common.form.FormUrlEncoded;

@Component
@HttpClient(configPath = "httpClient.default")
public interface FormHttpClient {

    @HttpRoute(method = HttpMethod.POST, path = "/form/encoded")
    HttpResponseEntity<String> formEncoded(FormUrlEncoded body);

    @HttpRoute(method = HttpMethod.POST, path = "/form/multipart")
    HttpResponseEntity<String> formMultipart(FormMultipart body);
}

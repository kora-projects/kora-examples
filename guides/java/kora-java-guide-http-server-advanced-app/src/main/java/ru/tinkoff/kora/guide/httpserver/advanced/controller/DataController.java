package ru.tinkoff.kora.guide.httpserver.advanced.controller;

import java.util.List;
import java.util.stream.Collectors;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.InterceptWith;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.http.common.form.FormUrlEncoded;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
@InterceptWith(DataApiAuthInterceptor.class)
public final class DataController {

    @HttpRoute(method = HttpMethod.POST, path = "/data/form")
    public String processForm(FormUrlEncoded formBody) {
        var namePart = formBody.get("name");
        var name = namePart == null || namePart.values().isEmpty() ? "World" : namePart.values().get(0);
        if ("admin".equalsIgnoreCase(name)) {
            throw new RestrictedFormNameException(name);
        }
        return "Hello World, " + name;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/data/upload")
    @Json
    public UploadResponse processUpload(FormMultipart multipart) {
        List<String> fileNames = multipart.parts().stream().map(FormMultipart.FormPart::name).sorted().collect(Collectors.toList());
        return new UploadResponse(fileNames.size(), fileNames);
    }

    @HttpRoute(method = HttpMethod.POST, path = "/data/mapping-request")
    public String processMappedRequest(String body) {
        return "Received mapped body: " + body;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/data/mapping-by-code/{code}")
    @Json
    public Payload mappingByCode(@Path int code) {
        if (code == 200) {
            return new Payload("Hello from response mapper");
        }
        throw HttpServerResponseException.of(code, "Request failed with code " + code);
    }

    @Json
    public record Payload(String message) {}

    @Json
    public record UploadResponse(int fileCount, List<String> fileNames) {}
}


package io.koraframework.example.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockserver.model.RegexBody;
import org.mockserver.model.StringBody;
import io.koraframework.http.client.common.request.HttpClientRequest;
import io.koraframework.http.client.common.request.HttpClientRequestBuilderImpl;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.form.FormMultipart;
import io.koraframework.http.common.form.FormUrlEncoded;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application.class)
class FormHttpClientTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private FormHttpClient httpClient;

    @Test
    void formEncodedHttpClient() {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withHeader("content-type", "application/x-www-form-urlencoded")
                        .withPath("/form/encoded")
                        .withBody(new StringBody("password=12345&name=Ivan")))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("OK"));

        // then
        var response = httpClient.formEncoded(new FormUrlEncoded(
                new FormUrlEncoded.FormPart("name", "Ivan"),
                new FormUrlEncoded.FormPart("password", "12345")));

        assertEquals(200, response.code());
        assertEquals("OK", response.body());
    }

    @Test
    void formMultipartHttpClient() {
        // given
        var requestBody = """
                --blob:.*\\r
                content-disposition: form-data; name="field1"\\r
                content-type: text/plain; charset=utf-8\\r
                \\r
                some data content\\r
                --blob:.*\\r
                content-disposition: form-data; name="field2"; filename="example1\\.txt"\\r
                content-type: text/plain\\r
                \\r
                some file content\\r
                --blob:.*""";

        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withHeader("content-type", "multipart/form-data;boundary=\".*\"")
                        .withPath("/form/multipart")
                        .withBody(new RegexBody(requestBody)))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody("OK"));

        // then
        var response = httpClient.formMultipart(new FormMultipart(List.of(
                FormMultipart.data("field1", "some data content"),
                FormMultipart.file("field2", "example1.txt", "text/plain",
                        "some file content".getBytes(StandardCharsets.UTF_8)))));

        HttpClientRequest request = new HttpClientRequestBuilderImpl("POST", "http://localhost:8090/pets/{petId}")
                .templateParam("petId", "1")
                .queryParam("page", 1)
                .header("token", "12345")
                .body(HttpBody.plaintext("refresh"))
                .build();

        assertEquals(200, response.code());
        assertEquals("OK", response.body());
    }
}

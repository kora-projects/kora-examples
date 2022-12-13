package ru.tinkoff.kora.example.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.okhttp.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MultipartControllerTests {

    @Container
    private static final AppContainer container = AppContainer.build();

    @Test
    void multipartController() throws Exception {
        // given
        var okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);

        var fileBody = RequestBody.create(MediaType.parse("application/text"),
                getClass().getClassLoader().getResourceAsStream("multipart-file.txt").readAllBytes());
        var multipart = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("key1", "value1")
                .addFormDataPart("key2", "value2")
                .addFormDataPart("file1", "", fileBody)
                .build();

        // then
        var request = new Request.Builder()
                .url(container.getURI().resolve("/multipart").toURL())
                .post(multipart)
                .build();
        var response = okHttpClient.newCall(request).execute();

        final String body = new String(response.body().bytes());
        assertEquals(200, response.code(), body);
        assertEquals("file1,key1,key2", body, body);
    }
}

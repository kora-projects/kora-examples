package ru.tinkoff.kora.guide.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.guide.httpclient.client.DataApiClient;
import ru.tinkoff.kora.guide.httpclient.client.ManualDataHttpClient;
import ru.tinkoff.kora.http.common.form.FormUrlEncoded;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@Testcontainers
@KoraAppTest(Application.class)
class HttpClientAdvancedAppTest implements KoraAppTestConfigModifier {

    @Container
    static final AppContainer APP = new AppContainer()
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer.class)));

    @TestComponent
    private DataApiClient dataApiClient;
    @TestComponent
    private ManualDataHttpClient manualDataHttpClient;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofResourceFile("application.conf")
                .withSystemProperty("DATA_API_URL", APP.getURI().toString());
    }

    @Test
    void dataClientCallsContainerizedHttpServerApp() {
        var formResponse = this.dataApiClient.processForm(new FormUrlEncoded(new FormUrlEncoded.FormPart("name", "John")));
        var uploadResponse = this.dataApiClient.sampleUpload();
        var mappedRequestResponse = this.dataApiClient.processMappedRequest(new DataApiClient.PlainTextGreetingBody("Client Mapper"));
        var mappedSuccess = this.dataApiClient.getMappedByCode(200);
        var mappedInternalError = this.dataApiClient.getMappedByCode(500);
        var mappedFailure = this.dataApiClient.getMappedByCode(404);
        var manualPing = this.manualDataHttpClient.pingManualHandler();

        assertEquals("Hello World, John", formResponse);
        assertEquals(2, uploadResponse.fileCount());
        assertTrue(uploadResponse.fileNames().contains("field1"));
        assertTrue(uploadResponse.fileNames().contains("field2"));
        assertEquals("Received mapped body: Hello Client Mapper", mappedRequestResponse);
        assertTrue(mappedSuccess instanceof DataApiClient.MappedResponse.Payload payload
                && "Hello from response mapper".equals(payload.message()));
        assertTrue(mappedInternalError instanceof DataApiClient.MappedResponse.Error error500
                && error500.code() == 500
                && "Request failed with code 500".equals(error500.message()));
        assertTrue(mappedFailure instanceof DataApiClient.MappedResponse.Error error404
                && error404.code() == 404
                && "Request failed with code 404".equals(error404.message()));
        assertEquals("manual-data-pong", manualPing);
    }
}

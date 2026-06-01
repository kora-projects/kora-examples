package ru.tinkoff.kora.guide.httpclient.controller;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.httpclient.client.DataApiClient;
import ru.tinkoff.kora.guide.httpclient.client.ManualDataHttpClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.form.FormUrlEncoded;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class ClientTestController {

    private final DataApiClient dataApiClient;
    private final ManualDataHttpClient manualDataHttpClient;

    public ClientTestController(DataApiClient dataApiClient, ManualDataHttpClient manualDataHttpClient) {
        this.dataApiClient = dataApiClient;
        this.manualDataHttpClient = manualDataHttpClient;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-data-endpoints")
    @Json
    public TestResults testAllDataEndpoints() {
        try {
            System.out.println("[http-client] Sending form body...");
            var formResult = this.dataApiClient.processForm(form("name", "John"));
            boolean formProcessed = "Hello World, John".equals(formResult);
            System.out.println("[http-client] Form finished: " + formProcessed);

            System.out.println("[http-client] Sending multipart body...");
            var uploadResult = this.dataApiClient.sampleUpload();
            boolean uploadProcessed = uploadResult.fileCount() == 2;
            System.out.println("[http-client] Multipart finished: " + uploadProcessed);

            System.out.println("[http-client] Sending custom mapped request body...");
            var mappedRequestResult = this.dataApiClient.processMappedRequest(new DataApiClient.PlainTextGreetingBody("Client Mapper"));
            boolean customRequestMapped = "Received mapped body: Hello Client Mapper".equals(mappedRequestResult);
            System.out.println("[http-client] Custom request mapping finished: " + customRequestMapped);

            System.out.println("[http-client] Reading mapped response with interceptor...");
            var mappedSuccess = this.dataApiClient.getMappedByCode(200);
            var mappedFailure = this.dataApiClient.getMappedByCode(404);
            boolean responseMapped = mappedSuccess instanceof DataApiClient.MappedResponse.Payload payload
                    && "Hello from response mapper".equals(payload.message())
                    && mappedFailure instanceof DataApiClient.MappedResponse.Error error
                    && error.code() == 404
                    && "Request failed with code 404".equals(error.message());
            System.out.println("[http-client] Response mapping finished: " + responseMapped);

            System.out.println("[http-client] Sending manual Kora HttpClient request...");
            var manualPingResult = this.manualDataHttpClient.pingManualHandler();
            boolean manualHttpClientCallProcessed = "manual-data-pong".equals(manualPingResult);
            System.out.println("[http-client] Manual HTTP client call finished: " + manualHttpClientCallProcessed);

            boolean allTestsPassed = formProcessed
                    && uploadProcessed
                    && customRequestMapped
                    && responseMapped
                    && manualHttpClientCallProcessed;
            return new TestResults(
                    formProcessed,
                    uploadProcessed,
                    customRequestMapped,
                    responseMapped,
                    manualHttpClientCallProcessed,
                    allTestsPassed,
                    null);
        } catch (Exception exception) {
            System.out.println("[http-client] Test flow failed: " + exception.getMessage());
            return new TestResults(false, false, false, false, false, false, exception.getMessage());
        }
    }

    private static FormUrlEncoded form(String... keyValues) {
        FormUrlEncoded.FormPart[] parts = new FormUrlEncoded.FormPart[keyValues.length / 2];
        for (int i = 0; i < keyValues.length; i += 2) {
            parts[i / 2] = new FormUrlEncoded.FormPart(keyValues[i], keyValues[i + 1]);
        }
        return new FormUrlEncoded(parts);
    }

    @Json
    public record TestResults(
            boolean formProcessed,
            boolean uploadProcessed,
            boolean customRequestMapped,
            boolean responseMapped,
            boolean manualHttpClientCallProcessed,
            boolean allTestsPassed,
            String error) {}
}

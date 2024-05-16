package ru.tinkoff.kora.example.soap.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer;
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection;
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockserver.model.XmlBody;
import ru.tinkoff.kora.example.generated.soap.SimpleService;
import ru.tinkoff.kora.example.generated.soap.TestRequest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersMockServer(mode = ContainerMode.PER_CLASS)
@KoraAppTest(Application.class)
class SimpleServiceTests implements KoraAppTestConfigModifier {

    @ConnectionMockServer
    private MockServerConnection mockserverConnection;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("SOAP_CLIENT_URL", mockserverConnection.params().uri().toString());
    }

    @TestComponent
    private SimpleService service;

    @Test
    void simpleServiceSuccess() throws Exception {
        // given
        mockserverConnection.client().when(
                org.mockserver.model.HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/")
                        .withBody(new XmlBody(
                                """
                                        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                        <ns2:Envelope xmlns:ns2="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns3="http://kora.tinkoff.ru/simple/service">
                                            <ns2:Header/>
                                            <ns2:Body>
                                                <ns3:TestRequest>
                                                    <val1>1</val1>
                                                    <val2>2</val2>
                                                </ns3:TestRequest>
                                            </ns2:Body>
                                        </ns2:Envelope>
                                        """)))
                .respond(
                        org.mockserver.model.HttpResponse.response()
                                .withBody(new XmlBody(
                                        """
                                                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                                                <ns2:Envelope xmlns:ns2="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns3="http://kora.tinkoff.ru/simple/service">
                                                    <ns2:Header/>
                                                    <ns2:Body>
                                                        <ns3:TestResponse>
                                                            <val1>1</val1>
                                                        </ns3:TestResponse>
                                                    </ns2:Body>
                                                </ns2:Envelope>
                                                """)));

        // then
        var request = new TestRequest();
        request.setVal1("1");
        request.setVal2("2");

        var response = service.test(request);
        assertEquals("1", response.getVal1());
    }
}

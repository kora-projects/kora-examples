package ru.tinkoff.kora.kotlin.example.soap.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.XmlBody
import ru.tinkoff.kora.example.generated.soap.SimpleService
import ru.tinkoff.kora.example.generated.soap.TestRequest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersMockServer(mode = ContainerMode.PER_CLASS)
@KoraAppTest(Application::class)
class SimpleServiceTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var service: SimpleService

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("SOAP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun simpleServiceSyncSuccess() {
        // given
        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withPath("/")
                .withBody(
                    XmlBody(
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
                        """.trimIndent()
                    )
                )
        ).respond(
            response()
                .withBody(
                    XmlBody(
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
                        """.trimIndent()
                    )
                )
        )

        // then
        val request = TestRequest()
        request.val1 = "1"
        request.val2 = "2"

        val response = service.test(request)
        assertEquals("1", response.val1)
    }
}

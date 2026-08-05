package io.koraframework.guide.httpclient

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import io.koraframework.guide.httpclient.client.DataApiClient
import io.koraframework.guide.httpclient.client.ManualDataHttpClient
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent

@Testcontainers
@KoraAppTest(Application::class)
class HttpClientAdvancedAppTest : KoraAppTestConfigModifier {
    @TestComponent
    lateinit var dataApiClient: DataApiClient

    @TestComponent
    lateinit var manualDataHttpClient: ManualDataHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofResourceFile("application.conf")
            .withSystemProperty("DATA_API_URL", APP.getURI().toString())

    @Test
    fun dataClientCallsContainerizedHttpServerApp() {
        val formResponse = dataApiClient.processForm(FormUrlEncoded(FormUrlEncoded.FormPart("name", "John")))
        val uploadResponse = dataApiClient.sampleUpload()
        val mappedRequestResponse =
            dataApiClient.processMappedRequest(DataApiClient.PlainTextGreetingBody("Client Mapper"))
        val mappedSuccess = dataApiClient.getMappedByCode(200)
        val mappedInternalError = dataApiClient.getMappedByCode(500)
        val mappedFailure = dataApiClient.getMappedByCode(404)
        val manualPing = manualDataHttpClient.pingManualHandler()

        assertEquals("Hello World, John", formResponse)
        assertEquals(2, uploadResponse.fileCount)
        assertTrue(uploadResponse.fileNames.contains("field1"))
        assertTrue(uploadResponse.fileNames.contains("field2"))
        assertEquals("Received mapped body: Hello Client Mapper", mappedRequestResponse)
        assertTrue(mappedSuccess is DataApiClient.MappedResponse.Payload && mappedSuccess.message == "Hello from response mapper")
        assertTrue(mappedInternalError is DataApiClient.MappedResponse.Error && mappedInternalError.code == 500 && mappedInternalError.message == "Request failed with code 500")
        assertTrue(mappedFailure is DataApiClient.MappedResponse.Error && mappedFailure.code == 404 && mappedFailure.message == "Request failed with code 404")
        assertEquals("manual-data-pong", manualPing)
    }

    companion object {
        @Container
        @JvmStatic
        val APP: AppContainer = AppContainer()
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }
}

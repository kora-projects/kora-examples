package ru.tinkoff.kora.guide.httpclient.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.httpclient.client.DataApiClient
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientTestController(
    private val dataApiClient: DataApiClient,
    private val manualDataHttpClient: ru.tinkoff.kora.guide.httpclient.client.ManualDataHttpClient
) {
    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-data-endpoints")
    @Json
    fun testAllDataEndpoints(): TestResults {
        return try {
            val formResult = dataApiClient.processForm(form("name", "John"))
            val formProcessed = formResult == "Hello World, John"

            val uploadResult = dataApiClient.sampleUpload()
            val uploadProcessed = uploadResult.fileCount == 2

            val mappedRequestResult =
                dataApiClient.processMappedRequest(DataApiClient.PlainTextGreetingBody("Client Mapper"))
            val customRequestMapped = mappedRequestResult == "Received mapped body: Hello Client Mapper"

            val mappedSuccess = dataApiClient.getMappedByCode(200)
            val mappedFailure = dataApiClient.getMappedByCode(404)
            val responseMapped =
                mappedSuccess is DataApiClient.MappedResponse.Payload &&
                        mappedSuccess.message == "Hello from response mapper" &&
                        mappedFailure is DataApiClient.MappedResponse.Error &&
                        mappedFailure.code == 404 &&
                        mappedFailure.message == "Request failed with code 404"

            val manualPingResult = manualDataHttpClient.pingManualHandler()
            val manualHttpClientCallProcessed = manualPingResult == "manual-data-pong"

            val allTestsPassed = formProcessed &&
                    uploadProcessed &&
                    customRequestMapped &&
                    responseMapped &&
                    manualHttpClientCallProcessed
            TestResults(
                formProcessed,
                uploadProcessed,
                customRequestMapped,
                responseMapped,
                manualHttpClientCallProcessed,
                allTestsPassed,
                null
            )
        } catch (e: Exception) {
            TestResults(false, false, false, false, false, false, e.message)
        }
    }

    private fun form(vararg keyValues: String): FormUrlEncoded {
        val parts = Array(keyValues.size / 2) { index ->
            FormUrlEncoded.FormPart(keyValues[index * 2], keyValues[index * 2 + 1])
        }
        return FormUrlEncoded(*parts)
    }

    @Json
    data class TestResults(
        val formProcessed: Boolean,
        val uploadProcessed: Boolean,
        val customRequestMapped: Boolean,
        val responseMapped: Boolean,
        val manualHttpClientCallProcessed: Boolean,
        val allTestsPassed: Boolean,
        val error: String?
    )
}

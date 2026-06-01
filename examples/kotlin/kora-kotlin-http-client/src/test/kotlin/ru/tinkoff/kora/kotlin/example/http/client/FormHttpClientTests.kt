package ru.tinkoff.kora.kotlin.example.http.client

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.mockserver.ConnectionMockServer
import io.goodforgod.testcontainers.extensions.mockserver.MockServerConnection
import io.goodforgod.testcontainers.extensions.mockserver.TestcontainersMockServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.RegexBody
import org.mockserver.model.StringBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.nio.charset.StandardCharsets

@TestcontainersMockServer(mode = ContainerMode.PER_RUN)
@KoraAppTest(Application::class)
class FormHttpClientTests : KoraAppTestConfigModifier {

    @ConnectionMockServer
    lateinit var mockserverConnection: MockServerConnection

    @TestComponent
    lateinit var httpClient: FormHttpClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("HTTP_CLIENT_URL", mockserverConnection.params().uri().toString())

    @Test
    fun formEncodedHttpClient() {
        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withHeader("content-type", "application/x-www-form-urlencoded")
                .withPath("/form/encoded")
                .withBody(StringBody("password=12345&name=Ivan"))
        ).respond(response().withBody("OK"))

        val response = httpClient.formEncoded(
            FormUrlEncoded(
                FormUrlEncoded.FormPart("name", "Ivan"),
                FormUrlEncoded.FormPart("password", "12345")
            )
        )

        assertEquals(200, response.code())
        assertEquals("OK", response.body())
    }

    @Test
    fun formMultipartHttpClient() {
        val requestBody = """
            --blob:.*\r
            content-disposition: form-data; name="field1"\r
            content-type: text/plain; charset=utf-8\r
            \r
            some data content\r
            --blob:.*\r
            content-disposition: form-data; name="field2"; filename="example1\.txt"\r
            content-type: text/plain\r
            \r
            some file content\r
            --blob:.*
        """.trimIndent()

        mockserverConnection.client().`when`(
            request()
                .withMethod("POST")
                .withHeader("content-type", "multipart/form-data;boundary=\".*\"")
                .withPath("/form/multipart")
                .withBody(RegexBody(requestBody))
        ).respond(response().withBody("OK"))

        val response = httpClient.formMultipart(
            FormMultipart(
                listOf(
                    FormMultipart.data("field1", "some data content"),
                    FormMultipart.file(
                        "field2",
                        "example1.txt",
                        "text/plain",
                        "some file content".toByteArray(StandardCharsets.UTF_8)
                    )
                )
            )
        )

        assertEquals(200, response.code())
        assertEquals("OK", response.body())
    }
}

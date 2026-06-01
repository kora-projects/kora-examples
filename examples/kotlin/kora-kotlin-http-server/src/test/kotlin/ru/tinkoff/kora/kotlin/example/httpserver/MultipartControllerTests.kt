package ru.tinkoff.kora.kotlin.example.httpserver

import com.squareup.okhttp.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.TimeUnit

@Testcontainers
class MultipartControllerTests {

    @Test
    fun multipartController() {
        val okHttpClient = OkHttpClient()
        okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS)

        val fileBody = RequestBody.create(
            MediaType.parse("application/text"),
            javaClass.classLoader.getResourceAsStream("multipart-file.txt")!!.readAllBytes()
        )
        val multipart = MultipartBuilder()
            .type(MultipartBuilder.FORM)
            .addFormDataPart("key1", "value1")
            .addFormDataPart("key2", "value2")
            .addFormDataPart("file1", "", fileBody)
            .build()

        val request = Request.Builder()
            .url(container.getURI().resolve("/multipart").toURL())
            .post(multipart)
            .build()
        val response = okHttpClient.newCall(request).execute()

        val body = String(response.body().bytes())
        assertEquals(200, response.code(), body)
        assertEquals("file1,key1,key2", body)
    }

    companion object {
        @Container
        private val container = AppContainer.build()
    }
}

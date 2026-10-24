package io.koraframework.guide.httpserver.advanced

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import io.koraframework.guide.httpserver.advanced.controller.DataController
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class HttpServerAdvancedAppTest {
    @TestComponent
    lateinit var dataController: DataController

    @Test
    fun componentsAreWired() {
        assertNotNull(dataController)
    }

    @Test
    fun formEndpointReturnsGreeting() {
        val form = FormUrlEncoded(FormUrlEncoded.FormPart("name", "John"))
        assertEquals("Hello World, John", dataController.processForm(form))
    }

    @Test
    fun mappedResponseEndpointReturnsPayload() {
        assertEquals("Hello from response mapper", dataController.mappingByCode(200).message)
    }
}

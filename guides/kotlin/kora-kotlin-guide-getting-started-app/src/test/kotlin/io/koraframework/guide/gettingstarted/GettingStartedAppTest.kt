package io.koraframework.guide.gettingstarted

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class GettingStartedAppTest {
    @TestComponent
    lateinit var helloController: HelloController

    @Test
    fun componentsAreWired() {
        assertNotNull(helloController)
    }

    @Test
    fun helloControllerReturnsPlainTextResponse() {
        val response = helloController.hello()
        assertEquals(200, response.code())
    }
}

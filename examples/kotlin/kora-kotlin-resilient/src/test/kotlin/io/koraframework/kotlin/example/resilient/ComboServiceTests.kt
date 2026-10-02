package io.koraframework.kotlin.example.resilient

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class ComboServiceTests {

    @TestComponent
    lateinit var comboService: ComboService

    @Test
    fun test() {
        assertNotNull(comboService.getValue(false))
    }
}

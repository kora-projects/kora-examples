package ru.tinkoff.kora.kotlin.example.resilient

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class ComboServiceTests {

    @TestComponent
    lateinit var comboService: ComboService

    @Test
    fun test() {
        assertNotNull(comboService.getValue(false))
    }
}

package ru.tinkoff.kora.kotlin.example.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import ru.tinkoff.kora.validation.common.ViolationException

@KoraAppTest(Application::class)
class ArgumentValidatorTests {

    @TestComponent
    lateinit var validator: ArgumentValidator

    @Test
    fun createSuccess() {
        // given
        val user = ArgumentUser("1", "Ivan", "2")
        val code = "ME2"

        // then
        val result = validator.calculate(user, 50, code)
        assertEquals(2, result)
    }

    @Test
    fun createFails() {
        // given
        val user = ArgumentUser("1", "Ivan", "2")
        val code = "2"

        // then
        assertThrows(ViolationException::class.java) { validator.calculate(user, 50, code) }
    }

    @Test
    fun createModelFails() {
        // given
        val user = ArgumentUser("1", "Mo", "2")
        val code = "ME2"

        // then
        assertThrows(ViolationException::class.java) { validator.calculate(user, 50, code) }
    }
}

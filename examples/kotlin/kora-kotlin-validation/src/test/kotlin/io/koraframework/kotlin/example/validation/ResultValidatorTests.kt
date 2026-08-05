package io.koraframework.kotlin.example.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent
import io.koraframework.validation.common.ViolationException

@KoraAppTest(Application::class)
class ResultValidatorTests {

    @TestComponent
    lateinit var validator: ResultValidator

    @Test
    fun createSuccess() {
        // given
        val name = "Ivan"
        val status = "2"

        // then
        val user = validator.create(name, status)
        assertNotNull(user)
        assertNotNull(user.id)
        assertEquals(name, user.name)
        assertEquals(status, user.status)
    }

    @Test
    fun createFails() {
        // given
        val name = "Mo"
        val status = "2"

        // then
        assertThrows(ViolationException::class.java) { validator.create(name, status) }
    }
}

package ru.tinkoff.kora.guide.dependencyinjection

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class DependencyInjectionGuideSmokeTest {

    @TestComponent
    private lateinit var notifyRunner: NotifyRunner

    @Test
    fun graphShouldStart() {
        assertNotNull(notifyRunner)
    }
}

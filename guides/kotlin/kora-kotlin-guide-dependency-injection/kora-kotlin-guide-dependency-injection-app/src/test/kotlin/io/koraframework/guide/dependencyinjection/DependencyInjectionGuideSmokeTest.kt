package io.koraframework.guide.dependencyinjection

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class DependencyInjectionGuideSmokeTest {

    @TestComponent
    private lateinit var notifyRunner: NotifyRunner

    @Test
    fun graphShouldStart() {
        assertNotNull(notifyRunner)
    }
}

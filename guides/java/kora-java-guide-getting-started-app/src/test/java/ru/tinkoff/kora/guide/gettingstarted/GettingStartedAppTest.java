package ru.tinkoff.kora.guide.gettingstarted;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class GettingStartedAppTest {

    @TestComponent
    private HelloController helloController;

    @Test
    void controllerIsWired() {
        assertNotNull(helloController);
    }
}

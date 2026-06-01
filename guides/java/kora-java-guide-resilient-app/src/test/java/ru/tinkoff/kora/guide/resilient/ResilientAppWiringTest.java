package ru.tinkoff.kora.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.resilient.controller.UserController;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class ResilientAppWiringTest {

    @TestComponent
    private UserController userController;
    @TestComponent
    private UserService userService;

    @Test
    void guideComponentsAreWired() {
        assertNotNull(userController);
        assertNotNull(userService);
    }
}

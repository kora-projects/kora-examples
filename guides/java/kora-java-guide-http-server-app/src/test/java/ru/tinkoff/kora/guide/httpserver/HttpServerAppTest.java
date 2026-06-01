package ru.tinkoff.kora.guide.httpserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.httpserver.controller.UserController;
import ru.tinkoff.kora.guide.httpserver.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class HttpServerAppTest {

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
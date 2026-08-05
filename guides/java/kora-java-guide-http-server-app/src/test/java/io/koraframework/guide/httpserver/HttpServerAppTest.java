package io.koraframework.guide.httpserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import io.koraframework.guide.httpserver.controller.UserController;
import io.koraframework.guide.httpserver.service.UserService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

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
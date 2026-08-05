package io.koraframework.example.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;
import io.koraframework.validation.common.ViolationException;

@KoraAppTest(Application.class)
class ResultValidatorTests {

    @TestComponent
    private ResultValidator validator;

    @Test
    void createSuccess() {
        // given
        var name = "Ivan";
        var status = "2";

        // then
        var user = validator.create(name, status);
        assertNotNull(user);
        assertNotNull(user.id());
        assertEquals(name, user.name());
        assertEquals(status, user.status());
    }

    @Test
    void createFails() {
        // given
        var name = "Mo";
        var status = "2";

        // then
        assertThrows(ViolationException.class, () -> validator.create(name, status));
    }
}

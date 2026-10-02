package io.koraframework.example.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;
import io.koraframework.validation.common.ViolationException;

@KoraAppTest(Application.class)
class ArgumentValidatorTests {

    @TestComponent
    private ArgumentValidator validator;

    @Test
    void createSuccess() {
        // given
        var user = new ArgumentValidator.User("1", "Ivan", "2");
        var code = "ME2";

        // then
        var result = validator.calculate(user, 50, code);
        assertEquals(2, result);
    }

    @Test
    void createFails() {
        // given
        var user = new ArgumentValidator.User("1", "Ivan", "2");
        var code = "2";

        // then
        assertThrows(ViolationException.class, () -> validator.calculate(user, 50, code));
    }

    @Test
    void createModelFails() {
        // given
        var user = new ArgumentValidator.User("1", "Mo", "2");
        var code = "ME2";

        // then
        assertThrows(ViolationException.class, () -> validator.calculate(user, 50, code));
    }
}

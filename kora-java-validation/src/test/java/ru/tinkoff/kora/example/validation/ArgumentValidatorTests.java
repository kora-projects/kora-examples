package ru.tinkoff.kora.example.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import ru.tinkoff.kora.validation.common.ViolationException;

@KoraAppTest(Application.class)
class ArgumentValidatorTests {

    @TestComponent
    private ArgumentValidator validator;

    @Test
    void createSuccess() {
        // given
        var user = new ArgumentValidator.User("1", "Bob", "2");
        var code = "ME2";

        // then
        var result = validator.calculate(user, 50, code);
        assertEquals(2, result);
    }

    @Test
    void createFails() {
        // given
        var user = new ArgumentValidator.User("1", "Bob", "2");
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

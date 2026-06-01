package ru.tinkoff.kora.example.resilient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class ComboServiceTests {

    @TestComponent
    private ComboService comboService;

    @Test
    void test() {
        assertNotNull(comboService.getValue(false));
    }
}

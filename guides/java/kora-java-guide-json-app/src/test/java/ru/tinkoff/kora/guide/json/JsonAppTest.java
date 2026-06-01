package ru.tinkoff.kora.guide.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.json.dto.UserRequest;
import ru.tinkoff.kora.guide.json.dto.UserResult;
import ru.tinkoff.kora.guide.json.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class JsonAppTest {

    @TestComponent
    private UserService userService;

    @Test
    void sealedJsonResponseFlow() {
        var created = userService.createUser(new UserRequest("Alice", "alice@example.com"));
        var result = userService.getUser(created.id());

        var success = assertInstanceOf(UserResult.UserSuccess.class, result);
        assertEquals(UserResult.Status.OK, success.status());
        assertEquals("Alice", success.user().name());
    }
}

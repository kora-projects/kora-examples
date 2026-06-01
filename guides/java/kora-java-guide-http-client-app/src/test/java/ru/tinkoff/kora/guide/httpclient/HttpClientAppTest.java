package ru.tinkoff.kora.guide.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.guide.httpclient.client.UserApiClient;
import ru.tinkoff.kora.guide.httpclient.dto.UserRequest;
import ru.tinkoff.kora.http.client.common.HttpClientResponseException;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@Testcontainers
@KoraAppTest(Application.class)
class HttpClientAppTest implements KoraAppTestConfigModifier {

    @Container
    static final AppContainer APP = new AppContainer()
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer.class)));

    @TestComponent
    private UserApiClient userApiClient;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofResourceFile("application.conf")
                .withSystemProperty("USER_API_URL", APP.getURI().toString());
    }

    @Test
    void createUserReturnsCreatedUserFromContainerizedHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var response = this.userApiClient.createUser(
                new UserRequest("Client User " + unique, "client-" + unique + "@example.com"),
                "request-1",
                "test-agent",
                "session-1");

        assertEquals(201, response.code());
        assertNotNull(response.body());
        assertEquals("Client User " + unique, response.body().name());
    }

    @Test
    void getUserReturnsUserResponseFromContainerizedHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userApiClient.createUser(
                new UserRequest("Lookup User " + unique, "lookup-" + unique + "@example.com"),
                "request-2",
                "test-agent",
                "session-2").body();

        var user = this.userApiClient.getUser(created.id());

        assertEquals(created.id(), user.id());
        assertEquals("Lookup User " + unique, user.name());
    }

    @Test
    void getMissingUserThrowsAgainstContainerizedHttpServerApp() {
        assertThrows(HttpClientResponseException.class, () -> this.userApiClient.getUser("999999"));
    }

    @Test
    void getUsersPassesPagingAndSortingAgainstContainerizedHttpServerApp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        var userA = this.userApiClient.createUser(new UserRequest("Alpha " + suffix, "a-" + suffix + "@example.com"), "req-a", "agent", "s1").body();
        var userB = this.userApiClient.createUser(new UserRequest("Bravo " + suffix, "b-" + suffix + "@example.com"), "req-b", "agent", "s2").body();
        var userC = this.userApiClient.createUser(new UserRequest("Charlie " + suffix, "c-" + suffix + "@example.com"), "req-c", "agent", "s3").body();

        var users = this.userApiClient.getUsers(0, 100, "name");
        var filtered = users.stream()
                .filter(user -> List.of(userA.id(), userB.id(), userC.id()).contains(user.id()))
                .toList();

        assertEquals(3, filtered.size());
        assertEquals(List.of("Alpha " + suffix, "Bravo " + suffix, "Charlie " + suffix),
                filtered.stream().map(ru.tinkoff.kora.guide.httpclient.dto.UserResponse::name).toList());
    }

    @Test
    void deleteUserReturnsNoContentFromContainerizedHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userApiClient.createUser(
                new UserRequest("Delete Me " + unique, "delete-" + unique + "@example.com"),
                "request-3",
                "test-agent",
                "session-3").body();

        var response = this.userApiClient.deleteUser(created.id());

        assertEquals(204, response.code());
    }

}

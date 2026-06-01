package ru.tinkoff.kora.guide.openapi.httpclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApi;
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApiResponses;
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserRequestTO;
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserResponseTO;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@Testcontainers
@KoraAppTest(Application.class)
class OpenApiHttpClientAppTest implements KoraAppTestConfigModifier {

    @Container
    static final AppContainer APP = new AppContainer()
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer.class)));

    @TestComponent
    private UsersApi usersApi;

    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofResourceFile("application.conf")
                .withSystemProperty("PUBLIC_API_URL", APP.getURI().toString());
    }

    @Test
    void createUserReturnsCreatedUserFromContainerizedOpenApiHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var response = this.usersApi.createUser(new UserRequestTO("Client User " + unique, "client-" + unique + "@example.com"));
        var create201 = assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class, response);

        assertNotNull(create201.content());
        assertEquals("Client User " + unique, create201.content().name());
    }

    @Test
    void getUserReturnsUserResponseFromContainerizedOpenApiHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = assertInstanceOf(
                UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class,
                this.usersApi.createUser(new UserRequestTO("Lookup User " + unique, "lookup-" + unique + "@example.com"))
        ).content();

        var response = this.usersApi.getUser(created.id());
        var getUser200 = assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse.class, response);

        assertEquals(created.id(), getUser200.content().id());
        assertEquals("Lookup User " + unique, getUser200.content().name());
    }

    @Test
    void getMissingUserReturnsNotFoundResponseFromContainerizedOpenApiHttpServerApp() {
        var response = this.usersApi.getUser("999999");
        assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse.class, response);
    }

    @Test
    void getUsersPassesPagingAndSortingAgainstContainerizedOpenApiHttpServerApp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        var userA = assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class,
                this.usersApi.createUser(new UserRequestTO("Alpha " + suffix, "a-" + suffix + "@example.com"))).content();
        var userB = assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class,
                this.usersApi.createUser(new UserRequestTO("Bravo " + suffix, "b-" + suffix + "@example.com"))).content();
        var userC = assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class,
                this.usersApi.createUser(new UserRequestTO("Charlie " + suffix, "c-" + suffix + "@example.com"))).content();

        var usersResponse = this.usersApi.getUsers(0, 100, "name");
        var users = assertInstanceOf(UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse.class, usersResponse).content();
        var filtered = users.stream()
                .filter(user -> List.of(userA.id(), userB.id(), userC.id()).contains(user.id()))
                .toList();

        assertEquals(3, filtered.size());
        assertEquals(List.of("Alpha " + suffix, "Bravo " + suffix, "Charlie " + suffix),
                filtered.stream().map(UserResponseTO::name).toList());
    }

    @Test
    void deleteUserReturnsNoContentResponseFromContainerizedOpenApiHttpServerApp() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = assertInstanceOf(
                UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class,
                this.usersApi.createUser(new UserRequestTO("Delete Me " + unique, "delete-" + unique + "@example.com"))
        ).content();

        var deleteResponse = this.usersApi.deleteUser(created.id());
        assertInstanceOf(UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse.class, deleteResponse);
    }
}



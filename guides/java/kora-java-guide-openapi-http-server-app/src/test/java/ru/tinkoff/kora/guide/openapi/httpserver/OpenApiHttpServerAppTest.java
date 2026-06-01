package ru.tinkoff.kora.guide.openapi.httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.openapi.httpserver.repository.UserRepository;
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiDelegate;
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiResponses;
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserRequestTO;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class OpenApiHttpServerAppTest {

    @TestComponent
    private UsersApiDelegate usersApiDelegate;

    @Test
    void crudFlowWorksThroughDelegate() throws Exception {
        var createResponse = this.usersApiDelegate.createUser(new UserRequestTO("John Doe", "john@example.com"));
        var create201 = assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse.class, createResponse);
        assertNotNull(create201.content());
        assertEquals("John Doe", create201.content().name());

        var getUserResponse = this.usersApiDelegate.getUser(create201.content().id());
        var getUser200 = assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse.class, getUserResponse);
        assertEquals("john@example.com", getUser200.content().email());

        var getUsersResponse = this.usersApiDelegate.getUsers(0, 10, "name");
        var getUsers200 = assertInstanceOf(UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse.class, getUsersResponse);
        assertEquals(1, getUsers200.content().size());

        var updateResponse = this.usersApiDelegate.updateUser(create201.content().id(), new UserRequestTO("John Updated", "john.updated@example.com"));
        var update200 = assertInstanceOf(UsersApiResponses.UpdateUserApiResponse.UpdateUser200ApiResponse.class, updateResponse);
        assertEquals("John Updated", update200.content().name());
        assertNotNull(update200.xUpdatedAt());

        var deleteResponse = this.usersApiDelegate.deleteUser(create201.content().id());
        assertInstanceOf(UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse.class, deleteResponse);

        var getAfterDeleteResponse = this.usersApiDelegate.getUser(create201.content().id());
        assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse.class, getAfterDeleteResponse);
    }
}



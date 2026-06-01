package ru.tinkoff.kora.guide.openapi.httpclient.controller;

import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApi;
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApiResponses;
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserRequestTO;
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserResponseTO;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class ClientTestController {

    private final UsersApi usersApi;

    public ClientTestController(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    public TestResults testAllUserEndpoints() {
        try {
            System.out.println("[openapi-http-client] Starting user create request...");
            var created = this.usersApi.createUser(new UserRequestTO("Client Demo User", "client-demo@example.com"));
            boolean userCreated = created instanceof UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse create201
                    && create201.content() != null;
            var createdUser = created instanceof UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse create201
                    ? create201.content()
                    : null;
            System.out.println("[openapi-http-client] Create finished: " + userCreated);

            System.out.println("[openapi-http-client] Fetching created user...");
            var getUserResponse = createdUser == null ? null : this.usersApi.getUser(createdUser.id());
            boolean userFetched = getUserResponse instanceof UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse getUser200
                    && createdUser.id().equals(getUser200.content().id());
            System.out.println("[openapi-http-client] Fetch finished: " + userFetched);

            System.out.println("[openapi-http-client] Listing users...");
            var getUsersResponse = this.usersApi.getUsers(0, 10, "name");
            var users = getUsersResponse instanceof UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse getUsers200
                    ? getUsers200.content()
                    : List.<UserResponseTO>of();
            boolean usersListed = createdUser != null && users.stream().anyMatch(user -> user.id().equals(createdUser.id()));
            System.out.println("[openapi-http-client] List finished: " + usersListed);

            System.out.println("[openapi-http-client] Deleting created user...");
            var deleteResult = createdUser == null ? null : this.usersApi.deleteUser(createdUser.id());
            boolean userDeleted = deleteResult instanceof UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse;
            System.out.println("[openapi-http-client] Delete finished: " + userDeleted);

            boolean allTestsPassed = userCreated && userFetched && usersListed && userDeleted;
            return new TestResults(userCreated, userFetched, usersListed, userDeleted, allTestsPassed, null);
        } catch (Exception exception) {
            System.out.println("[openapi-http-client] Test flow failed: " + exception.getMessage());
            return new TestResults(false, false, false, false, false, exception.getMessage());
        }
    }

    @Json
    public record TestResults(
            boolean userCreated,
            boolean userFetched,
            boolean usersListed,
            boolean userDeleted,
            boolean allTestsPassed,
            String error) {}
}



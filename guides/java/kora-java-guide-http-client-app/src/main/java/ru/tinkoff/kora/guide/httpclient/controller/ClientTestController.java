package ru.tinkoff.kora.guide.httpclient.controller;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.httpclient.client.UserApiClient;
import ru.tinkoff.kora.guide.httpclient.dto.UserRequest;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class ClientTestController {

    private final UserApiClient userApiClient;

    public ClientTestController(UserApiClient userApiClient) {
        this.userApiClient = userApiClient;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    public TestResults testAllUserEndpoints() {
        try {
            System.out.println("[http-client] Starting user create request...");
            var created = this.userApiClient.createUser(
                new UserRequest("Client Demo User", "client-demo@example.com"),
                "client-test-request",
                "guide-http-client-app",
                "client-test-session");

            boolean userCreated = created.code() == 201 && created.body() != null;
            var createdUser = created.body();
            System.out.println("[http-client] Create finished: " + userCreated);

            System.out.println("[http-client] Fetching created user...");
            var fetched = createdUser == null ? null : this.userApiClient.getUser(createdUser.id());
            boolean userFetched = fetched != null && createdUser != null && fetched.id().equals(createdUser.id());
            System.out.println("[http-client] Fetch finished: " + userFetched);

            System.out.println("[http-client] Listing users...");
            var users = this.userApiClient.getUsers(0, 10, "name");
            boolean usersListed = createdUser != null && users.stream().anyMatch(user -> user.id().equals(createdUser.id()));
            System.out.println("[http-client] List finished: " + usersListed);

            System.out.println("[http-client] Deleting created user...");
            var deleteResult = createdUser == null ? null : this.userApiClient.deleteUser(createdUser.id());
            boolean userDeleted = deleteResult != null && deleteResult.code() == 204;
            System.out.println("[http-client] Delete finished: " + userDeleted);

            boolean allTestsPassed = userCreated && userFetched && usersListed && userDeleted;
            return new TestResults(userCreated, userFetched, usersListed, userDeleted, allTestsPassed, null);
        } catch (Exception exception) {
            System.out.println("[http-client] Test flow failed: " + exception.getMessage());
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

package io.koraframework.guide.grpcclient.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.guide.grpcclient.dto.UserRequest;
import io.koraframework.guide.grpcclient.service.UserClientService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

@Component
@HttpController
public final class ClientTestController {

    private final UserClientService userClientService;

    public ClientTestController(UserClientService userClientService) {
        this.userClientService = userClientService;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    public TestResults testAllUserEndpoints() {
        try {
            System.out.println("[grpc-client] Creating user...");
            var created = this.userClientService.createUser(new UserRequest("Client Demo User", "client-demo@example.com"));
            boolean userCreated = created != null;

            System.out.println("[grpc-client] Fetching user...");
            var fetched = this.userClientService.getUser(created.id());
            boolean userFetched = created.id().equals(fetched.id());

            System.out.println("[grpc-client] Listing users...");
            var users = this.userClientService.getUsers(0, 10, "name");
            boolean usersListed = users.stream().anyMatch(user -> user.id().equals(created.id()));

            System.out.println("[grpc-client] Updating user...");
            var updated = this.userClientService.updateUser(created.id(),
                    new UserRequest("Updated Client Demo User", "updated-client-demo@example.com"));
            boolean userUpdated = "Updated Client Demo User".equals(updated.name());

            System.out.println("[grpc-client] Deleting user...");
            this.userClientService.deleteUser(created.id());
            boolean userDeleted = true;

            boolean allTestsPassed = userCreated && userFetched && usersListed && userUpdated && userDeleted;
            return new TestResults(userCreated, userFetched, usersListed, userUpdated, userDeleted, allTestsPassed, null);
        } catch (Exception exception) {
            return new TestResults(false, false, false, false, false, false, exception.getMessage());
        }
    }

    @Json
    public record TestResults(
            boolean userCreated,
            boolean userFetched,
            boolean usersListed,
            boolean userUpdated,
            boolean userDeleted,
            boolean allTestsPassed,
            String error) {}
}

package ru.tinkoff.kora.guide.grpcclient.advanced.controller;

import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserUpdateRequest;
import ru.tinkoff.kora.guide.grpcclient.advanced.service.UserStreamingClientService;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class ClientTestController {

    private final UserStreamingClientService userStreamingClientService;

    public ClientTestController(UserStreamingClientService userStreamingClientService) {
        this.userStreamingClientService = userStreamingClientService;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-streaming-endpoints")
    @Json
    public TestResults testAllStreamingEndpoints() {
        try {
            System.out.println("[grpc-client-advanced] Creating users via client streaming...");
            var created = this.userStreamingClientService.createUsers(List.of(
                    new UserRequest("Alice Streaming", "alice-streaming@example.com"),
                    new UserRequest("Bob Streaming", "bob-streaming@example.com")));
            boolean usersCreated = created.createdCount() == 2;

            System.out.println("[grpc-client-advanced] Reading users via server streaming...");
            var streamed = this.userStreamingClientService.getAllUsers();
            boolean usersStreamed = created.userIds().stream()
                    .allMatch(userId -> streamed.stream().anyMatch(user -> user.id().equals(userId)));

            System.out.println("[grpc-client-advanced] Updating users via bidirectional streaming...");
            var updated = this.userStreamingClientService.updateUsers(List.of(
                    new UserUpdateRequest(created.userIds().get(0), "Updated Alice Streaming", "updated-alice@example.com"),
                    new UserUpdateRequest(created.userIds().get(1), "Updated Bob Streaming", "updated-bob@example.com")));
            boolean usersUpdated = updated.stream().anyMatch(user -> "Updated Alice Streaming".equals(user.name()))
                    && updated.stream().anyMatch(user -> "Updated Bob Streaming".equals(user.name()));

            boolean allTestsPassed = usersCreated && usersStreamed && usersUpdated;
            return new TestResults(usersCreated, usersStreamed, usersUpdated, allTestsPassed, null);
        } catch (Exception exception) {
            return new TestResults(false, false, false, false, exception.getMessage());
        }
    }

    @Json
    public record TestResults(
            boolean usersCreated,
            boolean usersStreamed,
            boolean usersUpdated,
            boolean allTestsPassed,
            String error) {}
}

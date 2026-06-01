package ru.tinkoff.kora.example.grpc.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.generated.grpc.Message;
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc;

@Testcontainers
class GrpcServerTests {

    @Container
    private static final AppContainer appContainer = AppContainer.build()
            .withNetworkAliases("app")
            .withNetwork(Network.SHARED);

    @Test
    void createUser() {
        // given
        var event = Message.RequestEvent.newBuilder()
                .setName("bob")
                .setCode("b1")
                .build();

        // when
        var channel = ManagedChannelBuilder.forAddress(appContainer.getHost(), appContainer.getPort()).usePlaintext().build();
        var stub = UserServiceGrpc.newBlockingStub(channel);

        // then
        var response = stub.createUser(event);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(Message.ResponseEvent.Type.OPENED, response.getType());
        assertEquals(Message.ResponseEvent.StatusType.SUCCESS, response.getStatus());
    }
}

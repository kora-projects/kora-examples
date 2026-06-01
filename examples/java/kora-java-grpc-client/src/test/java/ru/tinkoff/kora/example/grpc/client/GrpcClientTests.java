package ru.tinkoff.kora.example.grpc.client;

import static org.junit.jupiter.api.Assertions.*;

import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.generated.grpc.Message;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class GrpcClientTests implements KoraAppTestConfigModifier {

    @TestComponent
    private RootService service;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("GRPC_URL", "grpc://localhost:8090");
    }

    @Test
    void createUser() {
        // given
        var event = Message.RequestEvent.newBuilder()
                .setName("bob")
                .setCode("b1")
                .build();

        // when
        var stub = service.service();
        assertThrows(StatusRuntimeException.class, () -> stub.createUser(event));
    }
}

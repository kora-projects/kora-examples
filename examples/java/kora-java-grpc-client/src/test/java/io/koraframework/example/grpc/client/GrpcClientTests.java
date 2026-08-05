package io.koraframework.example.grpc.client;

import static org.junit.jupiter.api.Assertions.*;

import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import io.koraframework.generated.grpc.Message;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

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

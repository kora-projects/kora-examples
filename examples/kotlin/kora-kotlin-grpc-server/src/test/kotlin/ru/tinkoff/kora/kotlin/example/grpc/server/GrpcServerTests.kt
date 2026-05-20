package ru.tinkoff.kora.kotlin.example.grpc.server

import io.grpc.ManagedChannelBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.generated.grpc.Message
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc

@Testcontainers
class GrpcServerTests {

    @Test
    fun createUser() {
        // given
        val event = Message.RequestEvent.newBuilder()
            .setName("bob")
            .setCode("b1")
            .build()

        // when
        val channel = ManagedChannelBuilder.forAddress(appContainer.host, appContainer.getPort()).usePlaintext().build()
        val stub = UserServiceGrpc.newBlockingStub(channel)

        // then
        val response = stub.createUser(event)
        assertNotNull(response)
        assertNotNull(response.id)
        assertEquals(Message.ResponseEvent.Type.OPENED, response.type)
        assertEquals(Message.ResponseEvent.StatusType.SUCCESS, response.status)
    }

    companion object {
        @Container
        private val appContainer = AppContainer.build()
            .withNetworkAliases("app")
            .withNetwork(Network.SHARED)
    }
}

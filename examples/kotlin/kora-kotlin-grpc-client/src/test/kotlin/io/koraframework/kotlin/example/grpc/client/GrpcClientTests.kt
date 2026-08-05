package io.koraframework.kotlin.example.grpc.client

import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import io.koraframework.generated.grpc.Message
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class GrpcClientTests : KoraAppTestConfigModifier {

    @TestComponent
    lateinit var service: RootService

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("GRPC_URL", "grpc://localhost:8090")

    @Test
    fun createUser() {
        // given
        val event = Message.RequestEvent.newBuilder()
            .setName("bob")
            .setCode("b1")
            .build()

        // when
        val stub = service.service()
        assertThrows(StatusRuntimeException::class.java) { stub.createUser(event) }
    }
}

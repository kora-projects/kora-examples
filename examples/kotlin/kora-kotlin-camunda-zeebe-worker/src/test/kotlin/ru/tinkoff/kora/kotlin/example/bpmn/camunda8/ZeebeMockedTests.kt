package ru.tinkoff.kora.kotlin.example.bpmn.camunda8

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.process.test.api.ZeebeTestEngine
import io.camunda.zeebe.process.test.assertions.BpmnAssert
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest
import org.junit.jupiter.api.Test
import org.mockito.Spy
import org.testcontainers.shaded.org.awaitility.Awaitility
import ru.tinkoff.kora.camunda.zeebe.worker.KoraZeebeJobWorkerEngine
import ru.tinkoff.kora.kotlin.example.camunda.zeebe.Application
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration
import java.util.Date
import java.util.UUID

@ZeebeProcessTest
@KoraAppTest(value = Application::class, components = [KoraZeebeJobWorkerEngine::class])
class ZeebeMockedTests : KoraAppTestConfigModifier {

    @Spy
    @TestComponent
    lateinit var client: ZeebeClient

    lateinit var engine: ZeebeTestEngine

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofSystemProperty("ZEEBE_GRPC_URL", client.configuration.grpcAddress.toString())

    @Test
    fun processDemoSuccess() {
        val deploymentEvent = client.newDeployResourceCommand()
            .addResourceFromClasspath("bpm/demo.bpmn")
            .send()
            .join()
        BpmnAssert.assertThat(deploymentEvent)

        val instanceEvent = client.newCreateInstanceCommand()
            .bpmnProcessId("demo")
            .latestVersion()
            .variables(
                """
                {"startId":"${UUID.randomUUID()}","b":"${Date()}"}
                """.trimIndent()
            )
            .send()
            .join()
        val instanceAssert = BpmnAssert.assertThat(instanceEvent)
        instanceAssert.isStarted()

        Awaitility.await()
            .pollInterval(Duration.ofSeconds(1))
            .atMost(Duration.ofSeconds(10))
            .until {
                try {
                    instanceAssert.isCompleted() != null
                } catch (_: Throwable) {
                    false
                }
            }
    }
}


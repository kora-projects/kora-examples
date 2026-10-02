package io.koraframework.kotlin.example.bpmn.camunda8

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.process.test.api.ZeebeTestEngine
import io.camunda.zeebe.process.test.assertions.BpmnAssert
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest
import org.junit.jupiter.api.Test
import org.mockito.Spy
import org.testcontainers.shaded.org.awaitility.Awaitility
import io.koraframework.camunda.zeebe.worker.KoraZeebeJobWorkerEngine
import io.koraframework.kotlin.example.camunda.zeebe.Application
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import java.time.Duration
import java.util.Date
import java.util.UUID

@ZeebeProcessTest
@KoraAppTest(value = Application::class, components = [KoraZeebeJobWorkerEngine::class])
class ZeebeMockedTests : KoraAppTestConfigModifier {

    // Is injected by ZeebeProcessTest. zeebe-process-test still exposes the legacy ZeebeClient
    // and its assertions accept only its response types; the application itself runs on CamundaClient,
    // so this must not be declared as a @TestComponent — it is not a component of the graph.
    @Spy
    lateinit var client: ZeebeClient

    // Is injected by ZeebeProcessTest
    lateinit var engine: ZeebeTestEngine

    override fun config(): KoraConfigModification =
        KoraConfigModification
            .ofSystemProperty("ZEEBE_GRPC_URL", client.configuration.grpcAddress.toString())
            .withSystemProperty("ZEEBE_REST_URL", client.configuration.restAddress.toString())

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


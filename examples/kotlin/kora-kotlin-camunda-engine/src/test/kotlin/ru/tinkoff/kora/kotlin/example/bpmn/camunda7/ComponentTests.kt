package ru.tinkoff.kora.kotlin.example.bpmn.camunda7

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.ProcessEngineAssert
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.testcontainers.shaded.org.awaitility.Awaitility
import ru.tinkoff.kora.camunda.engine.bpmn.CamundaEngineDataSource
import ru.tinkoff.kora.kotlin.example.camunda.engine.Application
import ru.tinkoff.kora.kotlin.example.camunda.engine.helloworld.LoggerDelegate
import ru.tinkoff.kora.test.extension.junit5.*
import java.time.Duration
import java.util.UUID

@KoraAppTest(Application::class)
class ComponentTests : KoraAppTestGraphModifier, KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    lateinit var mockDataSource: CamundaEngineDataSource

    @TestComponent
    lateinit var processEngine: ProcessEngine

    @Mock
    @TestComponent
    lateinit var loggerDelegate: LoggerDelegate

    override fun config(): KoraConfigModification = KoraConfigModification.ofString(
        """
        camunda.engine.bpmn {
          deployment.resources = "classpath:bpm"
        }
        """.trimIndent()
    )

    override fun graph(): KoraGraphModification = KoraGraphModification.create()
        .replaceComponent(ProcessEngineConfiguration::class.java, ::KoraAppTestInMemoryProcessEngineConfiguration)

    @Test
    fun processOnboardingCancelSuccess() {
        val businessKey = UUID.randomUUID().toString()
        val instance = processEngine.runtimeService.startProcessInstanceByKey("Onboarding", businessKey)
        assertNotNull(instance.id)

        BpmnAwareTests.assertThat(instance).isWaitingAt("ApproveOrderUserTaskId")
        assertDoesNotThrow {
            processEngine.runtimeService.correlateMessage("MessageCustomerCancellation", instance.businessKey)
        }
        ProcessEngineAssert.assertProcessEnded(processEngine, instance.processInstanceId)
    }

    @Test
    fun processOnboardingOrderSuccess() {
        val businessKey = UUID.randomUUID().toString()
        val instance = processEngine.runtimeService.startProcessInstanceByKey("Onboarding", businessKey)
        assertNotNull(instance.id)

        BpmnAwareTests.assertThat(instance).isWaitingAt("ApproveOrderUserTaskId")
        val tasks = processEngine.taskService.createTaskQuery()
            .processInstanceId(instance.processInstanceId)
            .active()
            .list()
        processEngine.formService.submitTaskForm(tasks[0].id, mapOf("approved" to true))

        Awaitility.waitAtMost(Duration.ofSeconds(10)).until {
            ProcessEngineAssert.assertProcessEnded(processEngine, instance.processInstanceId)
            true
        }
    }

    @Test
    fun processHelloWorldSuccess() {
        val businessKey = UUID.randomUUID().toString()
        val instance = processEngine.runtimeService.startProcessInstanceByKey("HelloWorld", businessKey)
        assertNotNull(instance.id)

        Awaitility.waitAtMost(Duration.ofSeconds(10)).until {
            Mockito.verify(loggerDelegate, Mockito.times(1)).execute(any())
            true
        }
    }
}


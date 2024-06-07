package ru.tinkoff.kora.example.bpmn.camunda7;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.test.ProcessEngineAssert;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.tinkoff.kora.camunda.engine.CamundaDataSource;
import ru.tinkoff.kora.example.camunda.engine.Application;
import ru.tinkoff.kora.example.camunda.engine.helloworld.LoggerDelegate;
import ru.tinkoff.kora.test.extension.junit5.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@KoraAppTest(Application.class)
class CamundaEngineMockedTests implements KoraAppTestGraphModifier, KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    private CamundaDataSource mockDataSource;
    @TestComponent
    private ProcessEngine processEngine;

    @Mock
    @TestComponent
    private LoggerDelegate loggerDelegate;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                camunda.engine {
                  deployment.resources = "classpath:bpm"
                }
                """);
    }

    @NotNull
    @Override
    public KoraGraphModification graph() {
        return KoraGraphModification.create()
                .replaceComponent(ProcessEngineConfiguration.class, KoraAppTestInMemoryProcessEngineConfiguration::new);
    }

    @Test
    void processOnboardingCancelSuccess() {
        final String businessKey = UUID.randomUUID().toString();
        final ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("Onboarding", businessKey);
        assertNotNull(instance.getId());

        BpmnAwareTests.assertThat(instance).isWaitingAt("ApproveOrderUserTaskId");
        assertDoesNotThrow(() -> processEngine.getRuntimeService().correlateMessage("MessageCustomerCancellation", instance.getBusinessKey()));
        ProcessEngineAssert.assertProcessEnded(processEngine, instance.getProcessInstanceId());
    }

    @Test
    void processOnboardingOrderSuccess() {
        final String businessKey = UUID.randomUUID().toString();
        final ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("Onboarding", businessKey);
        assertNotNull(instance.getId());

        BpmnAwareTests.assertThat(instance).isWaitingAt("ApproveOrderUserTaskId");
        final List<Task> tasks = processEngine.getTaskService().createTaskQuery().processInstanceId(instance.getProcessInstanceId()).active().list();
        processEngine.getFormService().submitTaskForm(tasks.get(0).getId(), Map.of("approved", true));

        Awaitility.waitAtMost(Duration.ofSeconds(10)).until(() -> {
            ProcessEngineAssert.assertProcessEnded(processEngine, instance.getProcessInstanceId());
            return true;
        });
    }

    @Test
    void processHelloWorldSuccess() {
        final String businessKey = UUID.randomUUID().toString();
        final ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("HelloWorld", businessKey);
        assertNotNull(instance.getId());

        Awaitility.waitAtMost(Duration.ofSeconds(10)).until(() -> {
            Mockito.verify(loggerDelegate, Mockito.times(1)).execute(Mockito.any());
            return true;
        });
    }
}

package ru.tinkoff.kora.example.bpmn.camunda7;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.tinkoff.kora.camunda.engine.CamundaDataSource;
import ru.tinkoff.kora.example.camunda.engine.Application;
import ru.tinkoff.kora.example.camunda.engine.helloworld.LoggerDelegate;
import ru.tinkoff.kora.test.extension.junit5.*;

import java.time.Duration;
import java.util.UUID;

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
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("Onboarding", businessKey);
        Assertions.assertNotNull(instance.getId());

        Assertions.assertDoesNotThrow(() -> processEngine.getRuntimeService().correlateMessage("MessageCustomerCancellation", instance.getBusinessKey()));
    }

    @Test
    void processOnboardingOrderSuccess() {
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("Onboarding", businessKey);
        Assertions.assertNotNull(instance.getId());

        Assertions.assertDoesNotThrow(() -> processEngine.getRuntimeService().correlateMessage("Message_Order", instance.getBusinessKey()));
    }

    @Test
    void processHelloWorldSuccess() {
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("HelloWorld", businessKey);
        Assertions.assertNotNull(instance.getId());

        Awaitility.waitAtMost(Duration.ofSeconds(15)).until(() -> {
            Mockito.verify(loggerDelegate, Mockito.times(1)).execute(Mockito.any());
            return true;
        });
    }
}

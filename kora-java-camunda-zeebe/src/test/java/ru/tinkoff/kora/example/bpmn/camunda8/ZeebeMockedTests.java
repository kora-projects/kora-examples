package ru.tinkoff.kora.example.bpmn.camunda8;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.testcontainer.*;
import io.camunda.zeebe.process.test.filters.RecordStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.kora.camunda.zeebe.worker.KoraZeebeJobWorkerEngine;
import ru.tinkoff.kora.example.camunda.zeebe.Application;
import ru.tinkoff.kora.example.camunda.zeebe.Step2JobWorker;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@ZeebeProcessTest
@Testcontainers
@KoraAppTest(value = Application.class, components = KoraZeebeJobWorkerEngine.class)
class ZeebeMockedTests {

    @Container
    private static final EngineContainer zeebeContainer = EngineContainer.getContainer();

    private static ZeebeClient zeebeClientFromEngine;
    private static ContainerizedEngine zeebeEngine;

    @Mock
    @TestComponent
    private Step2JobWorker step2JobWorker;
    @Mock
    @TestComponent
    private ZeebeClient zeebeClient;

    @BeforeAll
    static void setup() {
        if (zeebeClientFromEngine == null) {
            Integer containerPort = zeebeContainer.getMappedPort(ContainerProperties.getContainerPort());
            Integer gatewayPort = zeebeContainer.getMappedPort(ContainerProperties.getGatewayPort());
            zeebeEngine = new ContainerizedEngine(zeebeContainer.getHost(), containerPort, gatewayPort);
            zeebeEngine.start();
            zeebeClientFromEngine = zeebeEngine.createClient();
        }
    }

    @BeforeEach
    void setupTest() {
        zeebeEngine.start();

        RecordStream recordStream = RecordStream.of(new RecordStreamSourceImpl(zeebeEngine));
        BpmnAssert.initRecordStream(recordStream);
    }

    @AfterEach
    void cleanup() {
        zeebeEngine.reset();
    }

    @Test
    void processDemoSuccess() {

    }
}

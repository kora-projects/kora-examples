package ru.tinkoff.kora.example.bpmn.camunda8;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.DeploymentAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.tinkoff.kora.camunda.zeebe.worker.KoraZeebeJobWorkerEngine;
import ru.tinkoff.kora.example.camunda.zeebe.Application;
import ru.tinkoff.kora.example.camunda.zeebe.Step2JobWorker;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@ZeebeProcessTest
@KoraAppTest(value = Application.class, components = KoraZeebeJobWorkerEngine.class)
class ZeebeMockedTests implements KoraAppTestConfigModifier {

    // Is injected by ZeebeProcessTest
    @Spy
    @TestComponent
    private ZeebeClient client;

    // Is injected by ZeebeProcessTest
    private ZeebeTestEngine engine;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("ZEEBE_GRPC_URL", client.getConfiguration().getGrpcAddress().toString());
    }

    @Test
    void processDemoSuccess() {
        DeploymentEvent deploymentEvent = client.newDeployResourceCommand()
                .addResourceFromClasspath("bpm/demo.bpmn")
                .send()
                .join();
        DeploymentAssert deploymentAssert = BpmnAssert.assertThat(deploymentEvent);

        ProcessInstanceEvent instanceEvent = client.newCreateInstanceCommand()
                .bpmnProcessId("demo")
                .latestVersion()
                .variables("""
                        {"startId":"%s","b":"%s"}
                        """.formatted(UUID.randomUUID(), new Date()))
                .send()
                .join();
        ProcessInstanceAssert instanceAssert = BpmnAssert.assertThat(instanceEvent);
        instanceAssert.isStarted();

        Awaitility.await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(10))
                .until(() -> {
                    try {
                        return instanceAssert.isCompleted() != null;
                    } catch (Throwable e) {
                        return false;
                    }
                });
    }
}

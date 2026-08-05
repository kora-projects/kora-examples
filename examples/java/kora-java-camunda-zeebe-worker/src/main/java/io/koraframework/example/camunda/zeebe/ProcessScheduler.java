package io.koraframework.example.camunda.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.jdk.annotation.ScheduleAtFixedRate;

@Component
public final class ProcessScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessScheduler.class);

    private final ZeebeClient client;

    public ProcessScheduler(ZeebeClient client) {
        this.client = client;
    }

    @ScheduleAtFixedRate(period = 5000L, initialDelay = 500L)
    public void start() {
        final ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId("demo")
                .latestVersion()
                .variables("""
                        {"startId":"%s","b":"%s"}
                        """.formatted(UUID.randomUUID(), new Date()))
                .send()
                .join();

        logger.info("Started Process Instance for workflowKey={}, bpmnProcessId={}, version={} with workflowInstanceKey={}",
                event.getProcessDefinitionKey(),
                event.getBpmnProcessId(),
                event.getVersion(),
                event.getProcessInstanceKey());
    }
}

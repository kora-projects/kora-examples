package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import io.camunda.zeebe.client.ZeebeClient
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleAtFixedRate
import java.util.Date
import java.util.UUID

@Component
class ProcessScheduler(private val client: ZeebeClient) {
    private val logger = LoggerFactory.getLogger(ProcessScheduler::class.java)

    @ScheduleAtFixedRate(period = 5000L, initialDelay = 500L)
    fun start() {
        val event = client
            .newCreateInstanceCommand()
            .bpmnProcessId("demo")
            .latestVersion()
            .variables(
                """
                {"startId":"${UUID.randomUUID()}","b":"${Date()}"}
                """.trimIndent(),
            )
            .send()
            .join()

        logger.info(
            "Started Process Instance for workflowKey={}, bpmnProcessId={}, version={} with workflowInstanceKey={}",
            event.processDefinitionKey,
            event.bpmnProcessId,
            event.version,
            event.processInstanceKey,
        )
    }
}


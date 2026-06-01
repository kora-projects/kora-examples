package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import io.camunda.zeebe.client.api.response.ActivatedJob
import org.slf4j.Logger
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext
import java.time.Instant

object WorkerUtils {
    fun logJob(logger: Logger, job: ActivatedJob) {
        logger.info(
            """
            Complete Job - {}
            [processKey: {}]
            [elementId: {}]
            [deadline: {}]
            [variables: {}]
            """.trimIndent(),
            job.type,
            job.processInstanceKey,
            job.elementId,
            Instant.ofEpochMilli(job.deadline),
            job.variables,
        )
    }

    fun logJob(logger: Logger, job: JobContext) {
        logger.info(
            """
            Complete Job - {}
            [processKey: {}]
            [elementId: {}]
            [deadline: {}]
            [variables: {}]
            """.trimIndent(),
            job.jobType(),
            job.processInstanceKey(),
            job.elementId(),
            job.deadline(),
            job.variablesAsString(),
        )
    }
}

package io.koraframework.kotlin.example.camunda.zeebe

import org.slf4j.Logger
import io.koraframework.camunda.zeebe.worker.JobContext

object WorkerUtils {
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

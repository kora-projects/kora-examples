package io.koraframework.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import io.koraframework.camunda.zeebe.worker.JobContext
import io.koraframework.camunda.zeebe.worker.annotation.JobVariable
import io.koraframework.camunda.zeebe.worker.annotation.JobWorker
import io.koraframework.common.annotation.Component

@Component
class Step2JobWorker {
    private val logger = LoggerFactory.getLogger(javaClass)

    @JobWorker("bar")
    fun handle(@JobVariable("someUser") user: Step1JobWorker.User, context: JobContext): Map<String, Any> {
        logger.info("Received user - {}", user)
        WorkerUtils.logJob(logger, context)
        return mapOf("someNumber" to 42)
    }
}

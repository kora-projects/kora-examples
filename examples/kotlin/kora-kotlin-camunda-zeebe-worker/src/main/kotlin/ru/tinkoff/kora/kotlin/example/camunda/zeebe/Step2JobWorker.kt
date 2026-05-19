package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker
import ru.tinkoff.kora.common.Component

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

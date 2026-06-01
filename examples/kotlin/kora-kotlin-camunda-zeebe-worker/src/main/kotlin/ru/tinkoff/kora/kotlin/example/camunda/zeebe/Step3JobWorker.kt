package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext
import ru.tinkoff.kora.camunda.zeebe.worker.JobWorkerException
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker
import ru.tinkoff.kora.common.Component

@Component
class Step3JobWorker {
    private val logger = LoggerFactory.getLogger(javaClass)

    @JobWorker("fail")
    fun handle(@JobVariable someNumber: Int, context: JobContext) {
        logger.info("Received number - {}", someNumber)
        WorkerUtils.logJob(logger, context)
        throw JobWorkerException("DOESNT_WORK")
    }
}

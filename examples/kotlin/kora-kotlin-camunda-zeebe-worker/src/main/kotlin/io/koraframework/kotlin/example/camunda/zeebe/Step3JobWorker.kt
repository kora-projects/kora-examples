package io.koraframework.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import io.koraframework.camunda.zeebe.worker.JobContext
import io.koraframework.camunda.zeebe.worker.JobWorkerException
import io.koraframework.camunda.zeebe.worker.annotation.JobVariable
import io.koraframework.camunda.zeebe.worker.annotation.JobWorker
import io.koraframework.common.annotation.Component

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

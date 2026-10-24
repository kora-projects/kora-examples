package io.koraframework.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import io.koraframework.camunda.zeebe.worker.JobContext
import io.koraframework.camunda.zeebe.worker.annotation.JobVariable
import io.koraframework.camunda.zeebe.worker.annotation.JobWorker
import io.koraframework.common.annotation.Component
import io.koraframework.json.common.annotation.Json
import java.util.concurrent.ThreadLocalRandom

@Component
class Step1JobWorker {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Json
    data class User(val name: String, val code: Int)

    @JobWorker("foo")
    @JobVariable("someUser")
    fun handle(@JobVariable("startId") id: String, context: JobContext): User {
        logger.info("Received startId - {}", id)
        WorkerUtils.logJob(logger, context)
        return User("Ivan", ThreadLocalRandom.current().nextInt(1, 100))
    }
}

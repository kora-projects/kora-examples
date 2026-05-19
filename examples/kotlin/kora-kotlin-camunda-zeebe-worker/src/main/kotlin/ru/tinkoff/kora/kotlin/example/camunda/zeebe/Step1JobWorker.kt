package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.json.common.annotation.Json
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

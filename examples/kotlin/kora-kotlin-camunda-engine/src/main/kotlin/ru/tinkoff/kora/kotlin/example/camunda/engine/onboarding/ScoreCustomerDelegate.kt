package ru.tinkoff.kora.kotlin.example.camunda.engine.onboarding

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import java.util.concurrent.ThreadLocalRandom

@Component
class ScoreCustomerDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(ScoreCustomerDelegate::class.java)

    override fun execute(execution: DelegateExecution) {
        val scoring = ThreadLocalRandom.current().nextInt(1, 100)
        logger.info("Scored {} with result {}.", execution.businessKey, scoring)
        execution.setVariable("result", scoring)
    }
}

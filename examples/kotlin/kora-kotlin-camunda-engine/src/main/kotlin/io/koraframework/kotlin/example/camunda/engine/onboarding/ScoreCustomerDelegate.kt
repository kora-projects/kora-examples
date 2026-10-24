package io.koraframework.kotlin.example.camunda.engine.onboarding

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
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

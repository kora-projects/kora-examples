package io.koraframework.kotlin.example.camunda.engine.onboarding

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component

@Component
class CreateCustomer : JavaDelegate {
    private val logger = LoggerFactory.getLogger(CreateCustomer::class.java)

    override fun execute(execution: DelegateExecution) {
        logger.info(
            "Creating approved customer {} with result {}.",
            execution.businessKey,
            execution.getVariable("result")
        )
    }
}

package ru.tinkoff.kora.kotlin.example.camunda.engine.helloworld

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component

@Component
class LoggerDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(LoggerDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution) {
        logger.info("Hello World: {}", delegateExecution)
    }
}

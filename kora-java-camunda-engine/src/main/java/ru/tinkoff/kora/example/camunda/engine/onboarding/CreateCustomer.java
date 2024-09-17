package ru.tinkoff.kora.example.camunda.engine.onboarding;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;

@Component
public final class CreateCustomer implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateCustomer.class);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Creating approved customer {} with result {}.", execution.getBusinessKey(), execution.getVariable("result"));
    }
}

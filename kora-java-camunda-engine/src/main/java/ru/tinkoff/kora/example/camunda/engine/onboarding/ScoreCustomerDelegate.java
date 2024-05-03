package ru.tinkoff.kora.example.camunda.engine.onboarding;

import java.util.concurrent.ThreadLocalRandom;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;

@Component
public final class ScoreCustomerDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ScoreCustomerDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        int scoring = ThreadLocalRandom.current().nextInt(1, 100);
        logger.info("Scored {} with result {}.", execution.getBusinessKey(), scoring);
        execution.setVariable("result", scoring);
    }
}

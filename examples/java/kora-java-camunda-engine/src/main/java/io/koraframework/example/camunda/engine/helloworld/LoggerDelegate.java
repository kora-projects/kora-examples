package io.koraframework.example.camunda.engine.helloworld;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;

@Component
public final class LoggerDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LoggerDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        logger.info("Hello World: {}", delegateExecution);
    }
}

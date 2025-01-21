package ru.tinkoff.kora.example.camunda.zeebe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker;
import ru.tinkoff.kora.common.Component;

import java.util.Map;

import static ru.tinkoff.kora.example.camunda.zeebe.WorkerUtils.logJob;

@Component
public final class Step2JobWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @JobWorker("bar")
    public Map<String, Object> handle(@JobVariable("someUser") Step1JobWorker.User user, JobContext context) {
        logger.info("Received user - {}", user);
        logJob(logger, context);
        return Map.of("someNumber", 42);
    }
}

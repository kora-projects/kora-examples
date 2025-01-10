package ru.tinkoff.kora.example.camunda.zeebe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;
import ru.tinkoff.kora.camunda.zeebe.worker.JobWorkerException;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker;
import ru.tinkoff.kora.common.Component;

import java.util.Map;

import static ru.tinkoff.kora.example.camunda.zeebe.WorkerUtils.logJob;

@Component
public final class Step3JobWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @JobWorker("fail")
    public void handle(@JobVariable int someNumber, JobContext context) {
        logger.info("Received number - {}", someNumber);
        logJob(logger, context);
        throw new JobWorkerException("DOESNT_WORK");
    }
}

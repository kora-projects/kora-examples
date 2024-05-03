package ru.tinkoff.kora.example.camunda.zeebe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker;
import ru.tinkoff.kora.common.Component;

import static ru.tinkoff.kora.example.camunda.zeebe.WorkerUtils.logJob;

@Component
public final class Step1JobWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @JobWorker("foo")
    public void handle(JobContext context) {
        logJob(logger, context);
    }
}

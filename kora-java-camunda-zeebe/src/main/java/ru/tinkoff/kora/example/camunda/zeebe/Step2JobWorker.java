package ru.tinkoff.kora.example.camunda.zeebe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker;
import ru.tinkoff.kora.common.Component;

import java.util.Map;

import static ru.tinkoff.kora.example.camunda.zeebe.WorkerUtils.logJob;

@Component
public final class Step2JobWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @JobWorker("bar")
    public Map<String, String> handle(JobContext context) {
        logJob(logger, context);
        return Map.of("someResult", "42");
    }
}

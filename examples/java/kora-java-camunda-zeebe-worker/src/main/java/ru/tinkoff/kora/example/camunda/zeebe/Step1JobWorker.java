package ru.tinkoff.kora.example.camunda.zeebe;

import static ru.tinkoff.kora.example.camunda.zeebe.WorkerUtils.logJob;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobVariable;
import ru.tinkoff.kora.camunda.zeebe.worker.annotation.JobWorker;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
public final class Step1JobWorker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Json
    public record User(String name, int code) {}

    @JobWorker("foo")
    @JobVariable("someUser")
    public User handle(@JobVariable("startId") String id, JobContext context) {
        logger.info("Received startId - {}", id);
        logJob(logger, context);
        return new User("Ivan", ThreadLocalRandom.current().nextInt(1, 100));
    }
}

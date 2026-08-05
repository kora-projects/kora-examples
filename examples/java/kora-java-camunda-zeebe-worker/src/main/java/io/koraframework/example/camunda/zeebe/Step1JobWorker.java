package io.koraframework.example.camunda.zeebe;

import static io.koraframework.example.camunda.zeebe.WorkerUtils.logJob;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.camunda.zeebe.worker.JobContext;
import io.koraframework.camunda.zeebe.worker.annotation.JobVariable;
import io.koraframework.camunda.zeebe.worker.annotation.JobWorker;
import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;

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

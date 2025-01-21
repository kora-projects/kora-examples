package ru.tinkoff.kora.example.camunda.zeebe;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import ru.tinkoff.kora.camunda.zeebe.worker.JobContext;

import java.time.Instant;

public final class WorkerUtils {

    private WorkerUtils() { }

    public static void logJob(Logger logger, ActivatedJob job) {
        logger.info("""
                        Complete Job - {}
                        [processKey: {}]
                        [elementId: {}]
                        [deadline: {}]
                        [variables: {}]
                        """, job.getType(), job.getProcessInstanceKey(), job.getElementId(),
                Instant.ofEpochMilli(job.getDeadline()), job.getVariables());
    }

    public static void logJob(Logger logger, JobContext job) {
        logger.info("""
                        Complete Job - {}
                        [processKey: {}]
                        [elementId: {}]
                        [deadline: {}]
                        [variables: {}]
                        """, job.jobType(), job.processInstanceKey(), job.elementId(),
                job.deadline(), job.variablesAsString());
    }
}

package io.koraframework.example.camunda.zeebe;

import org.slf4j.Logger;
import io.koraframework.camunda.zeebe.worker.JobContext;

public final class WorkerUtils {

    private WorkerUtils() {}

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

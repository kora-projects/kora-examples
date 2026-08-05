package io.koraframework.example.camunda.zeebe;

import static io.koraframework.example.camunda.zeebe.WorkerUtils.logJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.camunda.zeebe.worker.JobContext;
import io.koraframework.camunda.zeebe.worker.JobWorkerException;
import io.koraframework.camunda.zeebe.worker.annotation.JobVariable;
import io.koraframework.camunda.zeebe.worker.annotation.JobWorker;
import io.koraframework.common.annotation.Component;

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

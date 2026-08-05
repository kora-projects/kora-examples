package io.koraframework.example.graalvm.crud.vertx.service;

import io.koraframework.resilient.retry.Retry;
import io.koraframework.resilient.retry.annotation.RetrySpec;

@RetrySpec("resilient.retry.pet")
public interface PetRetry extends Retry {

}

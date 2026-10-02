package io.koraframework.example.crud.service;

import io.koraframework.resilient.retry.Retry;
import io.koraframework.resilient.retry.annotation.RetrySpec;

@RetrySpec("resilient.retry.pet")
public interface PetRetry extends Retry {

}

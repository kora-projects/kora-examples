package io.koraframework.example.submodule.vet.service;

import io.koraframework.resilient.retry.Retry;
import io.koraframework.resilient.retry.annotation.RetrySpec;

@RetrySpec("resilient.retry.vet")
public interface VetRetry extends Retry {

}

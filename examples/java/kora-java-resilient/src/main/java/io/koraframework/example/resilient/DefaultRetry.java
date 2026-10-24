package io.koraframework.example.resilient;

import io.koraframework.resilient.retry.Retry;
import io.koraframework.resilient.retry.annotation.RetrySpec;

@RetrySpec("resilient.retry.default")
public interface DefaultRetry extends Retry {}

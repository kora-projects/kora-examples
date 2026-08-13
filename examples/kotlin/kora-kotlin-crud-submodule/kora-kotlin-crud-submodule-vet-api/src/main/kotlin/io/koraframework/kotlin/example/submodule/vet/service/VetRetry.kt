package io.koraframework.kotlin.example.submodule.vet.service

import io.koraframework.resilient.retry.Retry
import io.koraframework.resilient.retry.annotation.RetrySpec

@RetrySpec("resilient.retry.vet")
interface VetRetry : Retry

package io.koraframework.kotlin.example.submodule.pet.service

import io.koraframework.resilient.retry.Retry
import io.koraframework.resilient.retry.annotation.RetrySpec

@RetrySpec("resilient.retry.pet")
interface PetRetry : Retry

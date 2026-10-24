package io.koraframework.kotlin.example.crud.service

import io.koraframework.resilient.retry.Retry
import io.koraframework.resilient.retry.annotation.RetrySpec

@RetrySpec("resilient.retry.pet")
interface PetRetry : Retry

package io.koraframework.kotlin.example.resilient

import io.koraframework.resilient.retry.Retry
import io.koraframework.resilient.retry.annotation.RetrySpec

@RetrySpec("resilient.retry.default")
interface DefaultRetry : Retry

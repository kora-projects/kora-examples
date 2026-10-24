package io.koraframework.kotlin.example.resilient

import io.koraframework.resilient.timeout.Timeouter
import io.koraframework.resilient.timeout.annotation.TimeoutSpec

@TimeoutSpec("resilient.timeout.default")
interface DefaultTimeouter : Timeouter

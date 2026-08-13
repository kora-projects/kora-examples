package io.koraframework.guide.resilient.service

import io.koraframework.resilient.timeout.Timeouter
import io.koraframework.resilient.timeout.annotation.TimeoutSpec

@TimeoutSpec("resilient.timeout.default")
interface DefaultTimeouter : Timeouter

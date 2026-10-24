package io.koraframework.example.resilient;

import io.koraframework.resilient.timeout.Timeouter;
import io.koraframework.resilient.timeout.annotation.TimeoutSpec;

@TimeoutSpec("resilient.timeout.default")
public interface DefaultTimeouter extends Timeouter {}

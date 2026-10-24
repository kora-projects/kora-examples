package io.koraframework.example.submodule.vet.service;

import io.koraframework.resilient.timeout.Timeouter;
import io.koraframework.resilient.timeout.annotation.TimeoutSpec;

@TimeoutSpec("resilient.timeout.vet")
public interface VetTimeouter extends Timeouter {

}

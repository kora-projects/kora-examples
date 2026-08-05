package io.koraframework.example.graalvm.crud.vertx.service;

import io.koraframework.resilient.timeout.Timeouter;
import io.koraframework.resilient.timeout.annotation.TimeoutSpec;

@TimeoutSpec("resilient.timeout.pet")
public interface PetTimeouter extends Timeouter {

}

package io.koraframework.example.graalvm.crud.cassandra.service;

import io.koraframework.resilient.timeout.Timeouter;
import io.koraframework.resilient.timeout.annotation.TimeoutSpec;

@TimeoutSpec("resilient.timeout.pet")
public interface PetTimeouter extends Timeouter {

}

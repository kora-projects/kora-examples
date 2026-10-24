package io.koraframework.example.crud.service;

import io.koraframework.resilient.timeout.Timeouter;
import io.koraframework.resilient.timeout.annotation.TimeoutSpec;

@TimeoutSpec("resilient.timeout.pet")
public interface PetTimeouter extends Timeouter {

}

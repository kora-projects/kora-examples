package io.koraframework.kotlin.example.submodule.pet.service

import io.koraframework.resilient.timeout.Timeouter
import io.koraframework.resilient.timeout.annotation.TimeoutSpec

@TimeoutSpec("resilient.timeout.pet")
interface PetTimeouter : Timeouter

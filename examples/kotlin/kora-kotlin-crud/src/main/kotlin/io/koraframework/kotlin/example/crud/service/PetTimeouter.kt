package io.koraframework.kotlin.example.crud.service

import io.koraframework.resilient.timeout.Timeouter
import io.koraframework.resilient.timeout.annotation.TimeoutSpec

@TimeoutSpec("resilient.timeout.pet")
interface PetTimeouter : Timeouter

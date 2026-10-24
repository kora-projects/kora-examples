package io.koraframework.kotlin.example.submodule.app

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.kotlin.example.submodule.pet.PetModule
import io.koraframework.kotlin.example.submodule.vet.VetModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.micrometer.module.MetricsModule
import io.koraframework.openapi.management.OpenApiManagementModule
import io.koraframework.validation.module.ValidationModule

@KoraApp
interface Application : PetModule, VetModule, HoconConfigModule, LogbackModule, ValidationModule, JsonModule,
    MetricsModule, OpenApiManagementModule, UndertowPublicHttpServerModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

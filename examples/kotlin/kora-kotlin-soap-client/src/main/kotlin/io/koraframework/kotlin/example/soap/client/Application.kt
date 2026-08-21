package io.koraframework.kotlin.example.soap.client

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.ok.OkHttpClientModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.soap.client.common.SoapClientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, OkHttpClientModule, SoapClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

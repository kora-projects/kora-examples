package io.koraframework.kotlin.example.http.client

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.ok.OkHttpClientModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonModule, OkHttpClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

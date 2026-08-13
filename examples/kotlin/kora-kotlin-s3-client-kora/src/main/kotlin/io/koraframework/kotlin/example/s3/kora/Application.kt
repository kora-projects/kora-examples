package io.koraframework.kotlin.example.s3.kora

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.ok.OkHttpClientModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.s3.client.kora.KoraS3ClientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, KoraS3ClientModule, OkHttpClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

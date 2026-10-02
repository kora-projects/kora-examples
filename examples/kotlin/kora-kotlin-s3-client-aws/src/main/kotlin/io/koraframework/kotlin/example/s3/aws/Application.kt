package io.koraframework.kotlin.example.s3.aws

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.jdk.JdkHttpClientModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.s3.client.aws.AwsS3ClientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, AwsS3ClientModule, JdkHttpClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

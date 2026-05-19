package ru.tinkoff.kora.kotlin.example.s3.aws

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.s3.client.aws.AwsS3ClientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, AwsS3ClientModule, JdkHttpClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

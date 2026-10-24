package io.koraframework.guide.s3

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.client.jdk.JdkHttpClientModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.s3.client.aws.AwsS3ClientModule
import io.koraframework.s3.client.kora.KoraS3ClientModule

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    JdkHttpClientModule,
    AwsS3ClientModule,
    KoraS3ClientModule,
    UndertowPublicHttpServerModule

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}

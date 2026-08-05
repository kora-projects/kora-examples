package io.koraframework.guide.messaging.kafka

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.kafka.common.KafkaModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowPublicHttpServerModule,
    JsonModule,
    KafkaModule,
    LogbackModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

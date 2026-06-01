package ru.tinkoff.kora.guide.messaging.kafka

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.kafka.common.KafkaModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowHttpServerModule,
    JsonModule,
    KafkaModule,
    LogbackModule 

    fun main() {
        KoraApplication.run(ApplicationGraph::graph)
    }

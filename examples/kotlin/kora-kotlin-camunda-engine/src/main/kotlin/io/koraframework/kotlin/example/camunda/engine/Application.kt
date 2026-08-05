package io.koraframework.kotlin.example.camunda.engine

import io.koraframework.application.graph.KoraApplication
import io.koraframework.camunda.engine.bpmn.CamundaEngineBpmnModule
import io.koraframework.camunda.rest.undertow.CamundaRestUndertowModule
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    LogbackModule,
    JsonModule,
    UndertowPublicHttpServerModule,
    CamundaEngineBpmnModule,
    CamundaRestUndertowModule,
    JdbcDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}

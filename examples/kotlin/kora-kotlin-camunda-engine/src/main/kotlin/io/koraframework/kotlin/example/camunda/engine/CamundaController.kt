package io.koraframework.kotlin.example.camunda.engine

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.repository.ResourceDefinition
import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json
import java.util.UUID

@Component
@HttpController("/camunda")
class CamundaController(private val processEngine: ProcessEngine) {
    @Json
    data class CamundaProcess(val instanceId: String, val businessKey: String)

    @HttpRoute(method = HttpMethod.GET, path = "/name")
    fun name(): HttpResponseEntity<String> = HttpResponseEntity.of(200, processEngine.name)

    @HttpRoute(method = HttpMethod.GET, path = "/definitions")
    fun definitions(): HttpResponseEntity<String> {
        val definitions = processEngine.repositoryService.createProcessDefinitionQuery().list()
            .map(ResourceDefinition::getKey)
            .sorted()
            .joinToString(", ", "[ ", " ]")
        return HttpResponseEntity.of(200, definitions)
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/start/helloworld")
    fun startHelloWorldProcess(): HttpResponseEntity<CamundaProcess> {
        val businessKey = UUID.randomUUID().toString()
        val instance = processEngine.runtimeService.startProcessInstanceByKey("HelloWorld", businessKey)
        return HttpResponseEntity.of(200, CamundaProcess(instance.id, businessKey))
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/start/onboarding")
    fun startOnboarding(): HttpResponseEntity<CamundaProcess> {
        val businessKey = UUID.randomUUID().toString()
        val instance = processEngine.runtimeService.startProcessInstanceByKey("Onboarding", businessKey)
        return HttpResponseEntity.of(200, CamundaProcess(instance.id, businessKey))
    }
}


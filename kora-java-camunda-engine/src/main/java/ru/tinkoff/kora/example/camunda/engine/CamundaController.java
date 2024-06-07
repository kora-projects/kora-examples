package ru.tinkoff.kora.example.camunda.engine;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@HttpController("/camunda")
public final class CamundaController {

    @Json
    public record CamundaProcess(String instanceId, String businessKey) {
    }

    private final ProcessEngine processEngine;

    public CamundaController(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/name")
    public HttpResponseEntity<String> name() {
        return HttpResponseEntity.of(200, processEngine.getName());
    }

    @HttpRoute(method = HttpMethod.GET, path = "/definitions")
    public HttpResponseEntity<String> definitions() {
        return HttpResponseEntity.of(200, processEngine.getRepositoryService().createProcessDefinitionQuery().list().stream()
                .map(ResourceDefinition::getKey)
                .sorted()
                .collect(Collectors.joining(", ", "[ ", " ]")));
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/start/helloworld")
    public HttpResponseEntity<CamundaProcess> startHelloWorldProcess() {
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("HelloWorld", businessKey);
        return HttpResponseEntity.of(200, new CamundaProcess(instance.getId(), businessKey));
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/start/onboarding")
    public HttpResponseEntity<CamundaProcess> startOnboarding() {
        String businessKey = UUID.randomUUID().toString();
        ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("Onboarding", businessKey);
        return HttpResponseEntity.of(200, new CamundaProcess(instance.getId(), businessKey));
    }
}

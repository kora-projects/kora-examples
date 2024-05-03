package ru.tinkoff.kora.example.camunda.engine;

import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ResourceDefinition;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Component
@HttpController("/camunda")
public final class CamundaController {

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
                .collect(Collectors.joining(",")));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/start")
    public HttpResponseEntity<String> startHelloWorldProcess() {
        return HttpResponseEntity.of(200, processEngine.getRuntimeService().startProcessInstanceByKey("HelloWorld").getId());
    }
}

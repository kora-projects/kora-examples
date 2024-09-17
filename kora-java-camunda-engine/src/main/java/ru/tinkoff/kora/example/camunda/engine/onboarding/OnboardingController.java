package ru.tinkoff.kora.example.camunda.engine.onboarding;

import java.util.Map;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Component
@HttpController("/camunda/process/onboarding")
public class OnboardingController {

    private final FormService formService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;

    public OnboardingController(FormService formService, TaskService taskService, RuntimeService runtimeService) {
        this.formService = formService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
    }

    @HttpRoute(path = "/cancel/{businessKey}", method = HttpMethod.GET)
    public String customerCancellation(@Path String businessKey) {
        this.runtimeService.correlateMessage("MessageCustomerCancellation", businessKey);
        return "Cancelled: " + businessKey;
    }

    @HttpRoute(path = "/order/{businessKey}", method = HttpMethod.GET)
    public String customerOrder(@Path String businessKey) {
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).active().singleResult();
        formService.submitTaskForm(task.getId(), Map.of("approved", true));
        return "Approved: " + businessKey;
    }
}

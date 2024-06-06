package ru.tinkoff.kora.example.camunda.engine.onboarding;

import org.camunda.bpm.engine.RuntimeService;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Component
@HttpController("/camunda/process/onboarding")
public class OnboardingController {

    private final RuntimeService runtimeService;

    public OnboardingController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @HttpRoute(path = "/cancel/{businessKey}", method = HttpMethod.GET)
    public String customerCancellation(@Path String businessKey) {
        this.runtimeService.correlateMessage("MessageCustomerCancellation", businessKey);
        return "Cancelled: " + businessKey;
    }

    @HttpRoute(path = "/order/{businessKey}", method = HttpMethod.GET)
    public String customerOrder(@Path String businessKey) {
        this.runtimeService.correlateMessage("Message_Order", businessKey);
        return "Ordered: " + businessKey;
    }
}

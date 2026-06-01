package ru.tinkoff.kora.kotlin.example.camunda.engine.onboarding

import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.server.common.annotation.HttpController

@Component
@HttpController("/camunda/process/onboarding")
class OnboardingController(
    private val formService: FormService,
    private val taskService: TaskService,
    private val runtimeService: RuntimeService,
) {
    @HttpRoute(path = "/cancel/{businessKey}", method = HttpMethod.GET)
    fun customerCancellation(@Path businessKey: String): String {
        runtimeService.correlateMessage("MessageCustomerCancellation", businessKey)
        return "Cancelled: $businessKey"
    }

    @HttpRoute(path = "/order/{businessKey}", method = HttpMethod.GET)
    fun customerOrder(@Path businessKey: String): String {
        val task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).active().singleResult()
        formService.submitTaskForm(task.id, mapOf("approved" to true))
        return "Approved: $businessKey"
    }
}

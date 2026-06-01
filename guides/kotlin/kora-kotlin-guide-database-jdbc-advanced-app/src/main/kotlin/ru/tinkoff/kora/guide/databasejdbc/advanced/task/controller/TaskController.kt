package ru.tinkoff.kora.guide.databasejdbc.advanced.task.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.MessageResponse
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskRequest
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskResponse
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatusRequest
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.service.TaskService
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class TaskController(
    private val taskService: TaskService
) {

    @HttpRoute(method = HttpMethod.GET, path = "/tasks/assigned")
    @Json
    fun getTasksByAssignees(@Query("ids") ids: List<Long>?): List<TaskResponse.TaskAssigned> {
        return taskService.getTasksByAssignees(ids ?: emptyList())
    }

    @HttpRoute(method = HttpMethod.POST, path = "/tasks")
    @Json
    fun createTask(@Json request: TaskRequest): HttpResponseEntity<TaskResponse> {
        val tasks = taskService.createTasks(request.tasks)
        return HttpResponseEntity.of(201, HttpHeaders.of(), TaskResponse(tasks))
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/tasks/{taskId}/status")
    @Json
    fun updateStatus(@Path taskId: Long, @Json request: TaskStatusRequest): MessageResponse {
        taskService.updateStatus(taskId, request.status)
        return MessageResponse("OK")
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/tasks/{taskId}/assignee/{userId}")
    @Json
    fun assignTask(@Path taskId: Long, @Path userId: Long): MessageResponse {
        taskService.assignTask(taskId, userId)
        return MessageResponse("OK")
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/tasks/{taskId}/assignee")
    @Json
    fun unassignTask(@Path taskId: Long): MessageResponse {
        taskService.unassignTask(taskId)
        return MessageResponse("OK")
    }
}

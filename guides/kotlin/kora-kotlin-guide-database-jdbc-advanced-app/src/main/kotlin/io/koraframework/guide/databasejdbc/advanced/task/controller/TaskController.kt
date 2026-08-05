package io.koraframework.guide.databasejdbc.advanced.task.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.databasejdbc.advanced.task.dto.MessageResponse
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskRequest
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskResponse
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatusRequest
import io.koraframework.guide.databasejdbc.advanced.task.service.TaskService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

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

package io.koraframework.guide.databasejdbc.advanced.task.controller;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.databasejdbc.advanced.task.dto.MessageResponse;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskRequest;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskResponse;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatusRequest;
import io.koraframework.guide.databasejdbc.advanced.task.service.TaskService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.http.common.header.HttpHeaders;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

import java.util.List;

@Component
@HttpController
public final class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/tasks/assigned")
    @Json
    public List<TaskResponse.TaskAssigned> getTasksByAssignees(@Nullable @Query("ids") List<Long> ids) {
        return taskService.getTasksByAssignees(ids);
    }

    @HttpRoute(method = HttpMethod.POST, path = "/tasks")
    @Json
    public HttpResponseEntity<TaskResponse> createTask(@Json TaskRequest request) {
        var tasks = taskService.createTasks(request.tasks());
        return HttpResponseEntity.of(201, HttpHeaders.of(), new TaskResponse(tasks));
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/tasks/{taskId}/status")
    @Json
    public MessageResponse updateStatus(@Path Long taskId, @Json TaskStatusRequest request) {
        taskService.updateStatus(taskId, request.status());
        return new MessageResponse("OK");
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/tasks/{taskId}/assignee/{userId}")
    @Json
    public MessageResponse assignTask(@Path Long taskId, @Path Long userId) {
        taskService.assignTask(taskId, userId);
        return new MessageResponse("OK");
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/tasks/{taskId}/assignee")
    @Json
    public MessageResponse unassignTask(@Path Long taskId) {
        taskService.unassignTask(taskId);
        return new MessageResponse("OK");
    }
}

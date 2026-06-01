package ru.tinkoff.kora.guide.databasejdbc.advanced.task.service;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskRequest;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskResponse;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.TaskDAO;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.TaskRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public final class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse.TaskCreated> createTasks(List<TaskRequest.TaskCreate> taskCreates) {
        return taskRepository.getJdbcConnectionFactory().inTx(() -> {
            var assigneeIds = taskCreates.stream()
                    .map(TaskRequest.TaskCreate::userAssigneeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (!assigneeIds.isEmpty()) {
                var existingAssigneeIds = taskRepository.findExistingAssigneeId(assigneeIds);
                if (existingAssigneeIds.size() != assigneeIds.size()) {
                    var nonExistingAssigneeIds = assigneeIds.stream()
                            .filter(aid -> !existingAssigneeIds.contains(aid))
                            .toList();
                    throw HttpServerResponseException.of(404, "Assignee users not found: " + nonExistingAssigneeIds);
                }
            }

            var tasks = taskCreates.stream()
                    .map(t -> new TaskDAO(t.title(), TaskStatus.TODO, t.description(), t.userAssigneeId()))
                    .toList();
            var taskIds = taskRepository.insert(tasks);

            List<TaskResponse.TaskCreated> taskCreateds = new ArrayList<>();
            for (int i = 0; i < taskIds.size(); i++) {
                var taskId = taskIds.get(i);
                var task = tasks.get(i);
                LocalDateTime now = LocalDateTime.now();
                taskCreateds.add(new TaskResponse.TaskCreated(taskId, task.title(), task.description(), TaskStatus.TODO, task.userAssigneeId(), now));
            }

            return taskCreateds;
        });
    }

    public List<TaskResponse.TaskAssigned> getTasksByAssignees(List<Long> ids) {
        return taskRepository.findAssignedByAssigneeIds(ids).stream()
                .map(this::toResponseAssigned)
                .toList();
    }

    public void updateStatus(long id, TaskStatus status) {
        var updated = taskRepository.updateStatus(id, status);
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "Task not found");
        }
    }

    public void assignTask(long taskId, Long userId) {
        taskRepository.getJdbcConnectionFactory().inTx(() -> {
            var updated = taskRepository.updateAssignee(taskId, userId);
            if (updated.value() < 1) {
                throw HttpServerResponseException.of(404, "Task not found");
            }
        });
    }

    public void unassignTask(long taskId) {
        var updated = taskRepository.updateAssignee(taskId, null);
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "Task not found");
        }
    }

    private TaskResponse.TaskAssigned toResponseAssigned(TaskDAO.SelectAssigned task) {
        return new TaskResponse.TaskAssigned(
                task.id(),
                task.base().title(),
                task.base().description(),
                task.base().status(),
                new UserRequest(task.assigned().name(), task.assigned().email()),
                task.updatedAt());
    }
}

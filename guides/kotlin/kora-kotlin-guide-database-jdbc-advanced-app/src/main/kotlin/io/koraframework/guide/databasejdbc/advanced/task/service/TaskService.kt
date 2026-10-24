package io.koraframework.guide.databasejdbc.advanced.task.service

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.JdbcExecutor
import io.koraframework.guide.databasejdbc.advanced.dto.UserRequest
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskRequest
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskResponse
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus
import io.koraframework.guide.databasejdbc.advanced.task.repository.TaskDAO
import io.koraframework.guide.databasejdbc.advanced.task.repository.TaskRepository
import io.koraframework.http.server.common.response.HttpServerResponseException
import java.time.LocalDateTime

@Component
class TaskService(
    private val taskRepository: TaskRepository
) {

    fun createTasks(taskCreates: List<TaskRequest.TaskCreate>): List<TaskResponse.TaskCreated> {
        return taskRepository.executor().inTx(JdbcExecutor.SqlSupplier {
            val assigneeIds = taskCreates
                .mapNotNull { it.userAssigneeId }
                .distinct()

            if (assigneeIds.isNotEmpty()) {
                val existingAssigneeIds = taskRepository.findExistingAssigneeId(assigneeIds)
                if (existingAssigneeIds.size != assigneeIds.size) {
                    val nonExistingAssigneeIds = assigneeIds.filter { it !in existingAssigneeIds }
                    throw HttpServerResponseException.of(404, "Assignee users not found: $nonExistingAssigneeIds")
                }
            }

            val tasks = taskCreates.map {
                TaskDAO(it.title, TaskStatus.TODO, it.description, it.userAssigneeId)
            }
            val taskIds = taskRepository.insert(tasks)

            taskIds.mapIndexed { index, taskId ->
                val task = tasks[index]
                TaskResponse.TaskCreated(
                    taskId,
                    task.title,
                    task.description,
                    TaskStatus.TODO,
                    task.userAssigneeId,
                    LocalDateTime.now()
                )
            }
        })
    }

    fun getTasksByAssignees(ids: List<Long>): List<TaskResponse.TaskAssigned> {
        return taskRepository.findAssignedByAssigneeIds(ids)
            .map(::toResponseAssigned)
    }

    fun updateStatus(id: Long, status: TaskStatus) {
        val updated = taskRepository.updateStatus(id, status)
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "Task not found")
        }
    }

    fun assignTask(taskId: Long, userId: Long) {
        taskRepository.executor().inTx(JdbcExecutor.SqlRunnable {
            val updated = taskRepository.updateAssignee(taskId, userId)
            if (updated.value() < 1) {
                throw HttpServerResponseException.of(404, "Task not found")
            }
        })
    }

    fun unassignTask(taskId: Long) {
        val updated = taskRepository.updateAssignee(taskId, null)
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "Task not found")
        }
    }

    private fun toResponseAssigned(task: TaskDAO.SelectAssigned): TaskResponse.TaskAssigned {
        return TaskResponse.TaskAssigned(
            task.id,
            task.base.title,
            task.base.description,
            task.base.status,
            UserRequest(task.assigned.name, task.assigned.email),
            task.updatedAt
        )
    }
}

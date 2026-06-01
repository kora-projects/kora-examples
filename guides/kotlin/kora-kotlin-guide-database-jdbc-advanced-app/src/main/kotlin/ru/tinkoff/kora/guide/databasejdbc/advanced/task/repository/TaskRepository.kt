package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus

@Repository
interface TaskRepository : JdbcRepository {

    @Query("SELECT id FROM users WHERE id = ANY(:assigneeIds)")
    fun findExistingAssigneeId(assigneeIds: List<Long>): List<Long>

    @Query(
        """
        SELECT
          t.id AS task_id,
          t.created_at,
          t.updated_at,
          u.id AS assignee_id,
          u.name AS assignee_name,
          u.email AS assignee_email,
          u.created_at AS assignee_created_at,
          t.title,
          t.status,
          t.description,
          t.user_assignee_id
        FROM tasks t
        JOIN users u ON u.id = t.user_assignee_id
        WHERE t.user_assignee_id = ANY(:assigneeIds)
        ORDER BY t.id
        """
    )
    fun findAssignedByAssigneeIds(assigneeIds: List<Long>): List<TaskDAO.SelectAssigned>

    @Query("INSERT INTO %{entity#inserts} RETURNING id")
    @Id
    fun insert(@Batch entity: List<TaskDAO>): List<Long>

    @Query(
        """
        UPDATE tasks
        SET status = :status, updated_at = CURRENT_TIMESTAMP
        WHERE id = :id
        """
    )
    fun updateStatus(id: Long, status: TaskStatus): UpdateCount

    @Query(
        """
        UPDATE tasks
        SET user_assignee_id = :userAssigneeId, updated_at = CURRENT_TIMESTAMP
        WHERE id = :id
        """
    )
    fun updateAssignee(id: Long, userAssigneeId: Long?): UpdateCount
}

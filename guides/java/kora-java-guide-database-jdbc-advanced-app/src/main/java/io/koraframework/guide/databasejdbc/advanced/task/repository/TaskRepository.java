package io.koraframework.guide.databasejdbc.advanced.task.repository;

import org.jspecify.annotations.Nullable;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Batch;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JdbcRepository {

    @Query("SELECT id FROM users WHERE id = ANY(:assigneeIds)")
    List<Long> findExistingAssigneeId(List<Long> assigneeIds);

    @Query("""
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
            """)
    List<TaskDAO.SelectAssigned> findAssignedByAssigneeIds(List<Long> assigneeIds);

    @Query("INSERT INTO %{entity#inserts} RETURNING id")
    @Id
    List<Long> insert(@Batch List<TaskDAO> entity);

    @Query("""
            UPDATE tasks
            SET status = :status, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            """)
    UpdateCount updateStatus(long id, TaskStatus status);

    @Query("""
            UPDATE tasks
            SET user_assignee_id = :userAssigneeId, updated_at = CURRENT_TIMESTAMP
            WHERE id = :id
            """)
    UpdateCount updateAssignee(long id, @Nullable Long userAssigneeId);
}

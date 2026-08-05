package io.koraframework.guide.databasecassandra.repository;

import org.jspecify.annotations.Nullable;
import java.util.List;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.cassandra.CassandraRepository;

@Repository
public interface UserRepository extends CassandraRepository {

    @Query("SELECT id, name, email, created_at FROM users")
    List<UserDAO> findAll();

    @Query("SELECT id, name, email, created_at FROM users WHERE id = :id")
    @Nullable
    UserDAO findById(String id);

    @Query("""
            INSERT INTO users(id, name, email, created_at)
            VALUES (:user.id, :user.name, :user.email, :user.createdAt)
            """)
    void save(UserDAO user);

    @Query("""
            UPDATE users
            SET name = :user.name, email = :user.email, created_at = :user.createdAt
            WHERE id = :user.id
            """)
    void update(UserDAO user);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(String id);
}


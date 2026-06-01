package ru.tinkoff.kora.guide.databasecassandra.repository;

import jakarta.annotation.Nullable;
import java.util.List;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;

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


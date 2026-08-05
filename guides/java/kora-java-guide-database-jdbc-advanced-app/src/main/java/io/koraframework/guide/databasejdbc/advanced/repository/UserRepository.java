package io.koraframework.guide.databasejdbc.advanced.repository;

import java.util.List;
import java.util.Optional;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface UserRepository extends JdbcRepository {

    @Query("SELECT id, name, email, created_at FROM users ORDER BY id")
    List<UserDAO> findAll();

    @Query("SELECT id, name, email, created_at FROM users WHERE id = :id")
    Optional<UserDAO> findById(Long id);

    @Query("INSERT INTO users(name, email) VALUES (:name, :email) RETURNING id")
    long save(String name, String email);

    @Query("UPDATE users SET name = :name, email = :email WHERE id = :id")
    UpdateCount update(Long id, String name, String email);

    @Query("DELETE FROM users WHERE id = :id")
    UpdateCount deleteById(Long id);
}


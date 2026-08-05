package io.koraframework.guide.databasejdbc.advanced.repository;

import java.time.LocalDateTime;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
public record UserDAO(
        @Column("id") Long id,
        @Column("name") String name,
        @Column("email") String email,
        @Column("created_at") LocalDateTime createdAt) {}

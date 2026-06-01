package ru.tinkoff.kora.guide.databasejdbc.advanced.repository;

import java.time.LocalDateTime;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;

@EntityJdbc
public record UserDAO(
        @Column("id") Long id,
        @Column("name") String name,
        @Column("email") String email,
        @Column("created_at") LocalDateTime createdAt) {}

package ru.tinkoff.kora.guide.databasecassandra.repository;

import java.time.Instant;
import ru.tinkoff.kora.database.cassandra.annotation.EntityCassandra;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Table;

@EntityCassandra
@Table("users")
public record UserDAO(
        @Column("id") String id,
        @Column("name") String name,
        @Column("email") String email,
        @Column("created_at") Instant createdAt) {}

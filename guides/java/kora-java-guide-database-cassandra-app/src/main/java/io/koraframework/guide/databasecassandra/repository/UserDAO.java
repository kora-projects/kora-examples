package io.koraframework.guide.databasecassandra.repository;

import java.time.Instant;
import io.koraframework.database.cassandra.annotation.EntityCassandra;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Table;

@EntityCassandra
@Table("users")
public record UserDAO(
        @Column("id") String id,
        @Column("name") String name,
        @Column("email") String email,
        @Column("created_at") Instant createdAt) {}

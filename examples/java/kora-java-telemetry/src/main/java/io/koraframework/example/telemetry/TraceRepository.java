package io.koraframework.example.telemetry;

import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;

@Repository
public interface TraceRepository extends JdbcRepository {
    @Query("SELECT 1")
    int selectOne();
}

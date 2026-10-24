package io.koraframework.example.cassandra;

import org.jspecify.annotations.Nullable;
import io.koraframework.database.cassandra.CassandraRepository;
import io.koraframework.database.cassandra.annotation.EntityCassandra;
import io.koraframework.database.cassandra.annotation.UDT;
import io.koraframework.database.common.annotation.*;

@Repository
public interface CassandraUdtRepository extends CassandraRepository {

    @EntityCassandra
    record Entity(String id, Name name) {

        @UDT
        public record Name(String first, String last) {}
    }

    @Query("SELECT * FROM entities_udt WHERE id = :id")
    @Nullable
    Entity findById(String id);

    @Query("""
            INSERT INTO entities_udt(id, name)
            VALUES (:entity.id, :entity.name)
            """)
    void insert(Entity entity);
}

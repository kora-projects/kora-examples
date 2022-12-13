package ru.tinkoff.kora.example.cassandra;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.database.cassandra.CassandraRepository;
import ru.tinkoff.kora.database.cassandra.annotation.UDT;
import ru.tinkoff.kora.database.common.annotation.*;

@Repository
public interface CassandraUdtRepository extends CassandraRepository {

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

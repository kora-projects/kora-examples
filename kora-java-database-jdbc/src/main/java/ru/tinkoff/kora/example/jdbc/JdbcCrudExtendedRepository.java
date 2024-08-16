package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.example.jdbc.JdbcCrudExtendedRepository.Entity;

@Repository
public interface JdbcCrudExtendedRepository extends AbstractJdbcCrudRepository<String, Entity> {

    @Table("entities")
    record Entity(@Id String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {
    }

    @Query("DELETE FROM entities WHERE id = :id")
    UpdateCount deleteById(String id);

    @Query("DELETE FROM entities")
    UpdateCount deleteAll();
}

package ru.tinkoff.kora.example.jdbc;

import jakarta.annotation.Nullable;
import java.util.Optional;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.example.jdbc.JdbcCrudExtendedRepository.Entity;

@Repository
public interface JdbcCrudExtendedRepository extends AbstractJdbcCrudRepository<String, Entity> {

    @EntityJdbc
    @Table("entities")
    record Entity(@Id String id,
                  @Column("value1") int field1,
                  String value2,
                  @Nullable String value3) {}

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<Entity> findById(String id);

    @Query("DELETE FROM entities WHERE id = :id")
    UpdateCount deleteById(String id);

    @Query("DELETE FROM entities")
    UpdateCount deleteAll();
}

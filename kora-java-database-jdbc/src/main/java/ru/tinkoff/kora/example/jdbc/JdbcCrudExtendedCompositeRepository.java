package ru.tinkoff.kora.example.jdbc;

import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.*;
import ru.tinkoff.kora.example.jdbc.JdbcCrudExtendedCompositeRepository.Entity;
import ru.tinkoff.kora.example.jdbc.JdbcCrudExtendedCompositeRepository.Entity.EntityId;

import java.util.UUID;

@Repository
public interface JdbcCrudExtendedCompositeRepository extends AbstractJdbcCrudRepository<EntityId, Entity> {

    @Table("entities_composite_uuid")
    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public record EntityId(UUID a, UUID b) {

            public EntityId() {
                this(UUID.randomUUID(), UUID.randomUUID());
            }
        }
    }

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    UpdateCount deleteById(Entity.EntityId id);

    @Query("DELETE FROM entities_composite_uuid")
    UpdateCount deleteAll();
}

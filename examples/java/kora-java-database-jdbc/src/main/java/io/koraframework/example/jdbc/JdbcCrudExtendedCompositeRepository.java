package io.koraframework.example.jdbc;

import java.util.Optional;
import java.util.UUID;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.*;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.example.jdbc.JdbcCrudExtendedCompositeRepository.Entity;
import io.koraframework.example.jdbc.JdbcCrudExtendedCompositeRepository.Entity.EntityId;

@Repository
public interface JdbcCrudExtendedCompositeRepository extends AbstractJdbcCrudRepository<EntityId, Entity> {

    @EntityJdbc
    @Table("entities_composite_uuid")
    record Entity(@Id @Embedded EntityId id,
                  @Column("name") String name) {

        public record EntityId(UUID a, UUID b) {

            public EntityId() {
                this(UUID.randomUUID(), UUID.randomUUID());
            }
        }
    }

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE %{id#where}")
    Optional<Entity> findById(EntityId id);

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    UpdateCount deleteById(Entity.EntityId id);

    @Query("DELETE FROM entities_composite_uuid")
    UpdateCount deleteAll();
}

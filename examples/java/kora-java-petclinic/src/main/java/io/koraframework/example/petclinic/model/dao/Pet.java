package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import java.time.LocalDate;

@EntityJdbc
@Table("pets")
public record Pet(@Id long id,
                  @Column("owner_id") long ownerId,
                  @Column("type_id") long typeId,
                  String name,
                  @Column("birth_date") LocalDate birthDate) {}

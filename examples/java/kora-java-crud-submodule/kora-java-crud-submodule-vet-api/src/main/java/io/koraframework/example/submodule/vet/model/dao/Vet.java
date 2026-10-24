package io.koraframework.example.submodule.vet.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
@Table("vets")
public record Vet(@Column("id") @Id long id,
                  @Column("name") String name,
                  @Column("name") String surname) {

}

package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
@Table("vets")
public record Vet(@Id long id,
                  @Column("first_name") String firstName,
                  @Column("last_name") String lastName) {}

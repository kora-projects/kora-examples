package io.koraframework.example.petclinic.model.dao;

import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
@Table("pet_types")
public record PetType(@Id long id, String name) {}

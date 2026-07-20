package ru.tinkoff.kora.example.crud.model.dao;

import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
@Table("categories")
public record PetCategory(@Id long id,
                          String name) {}

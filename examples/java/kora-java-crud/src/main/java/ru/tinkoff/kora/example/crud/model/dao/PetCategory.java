package ru.tinkoff.kora.example.crud.model.dao;

import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Table;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;

@EntityJdbc
@Table("categories")
public record PetCategory(@Id long id,
                          String name) {}

package ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao;

import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Table;

@Table("categories")
public record PetCategory(@Id long id,
                          String name) {}

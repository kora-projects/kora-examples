package io.koraframework.example.graalvm.crud.vertx.model.dao;

import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;

@Table("categories")
public record PetCategory(@Id long id,
                          String name) {}

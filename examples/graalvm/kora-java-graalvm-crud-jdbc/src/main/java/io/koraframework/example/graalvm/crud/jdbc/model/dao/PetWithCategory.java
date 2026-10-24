package io.koraframework.example.graalvm.crud.jdbc.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Embedded;
import io.koraframework.database.jdbc.annotation.EntityJdbc;

@EntityJdbc
public record PetWithCategory(@Column("id") long id,
                              @Column("name") String name,
                              Pet.@Column("status") Status status,
                              @Embedded("category_") PetCategory category) {

    public Pet getPet() {
        return new Pet(id, name, status, category.id());
    }
}

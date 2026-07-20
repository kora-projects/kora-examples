package ru.tinkoff.kora.example.crud.model.dao;

import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Embedded;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import ru.tinkoff.kora.example.crud.model.dao.Pet.Status;

@EntityJdbc
public record PetWithCategory(@Column("id") long id,
                              @Column("name") String name,
                              @Column("status") Status status,
                              @Embedded("category_") PetCategory category) {

    public Pet getPet() {
        return new Pet(id, name, status, category.id());
    }
}

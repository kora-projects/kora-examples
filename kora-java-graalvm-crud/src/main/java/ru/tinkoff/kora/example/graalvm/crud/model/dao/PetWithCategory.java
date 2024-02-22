package ru.tinkoff.kora.example.graalvm.crud.model.dao;

import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Embedded;

public record PetWithCategory(@Column("id") long id,
                              @Column("name") String name,
                              @Column("status") Pet.Status status,
                              @Embedded("category_") PetCategory category) {

    public Pet getPet() {
        return new Pet(id, name, status, category.id());
    }
}

package ru.tinkoff.kora.example.submodule.vet.model.dao;

import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Table;

@Table("vets")
public record Vet(@Column("id") @Id long id,
                  @Column("name") String name,
                  @Column("name") String surname) {

}

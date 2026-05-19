package ru.tinkoff.kora.kotlin.example.submodule.vet.model.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import ru.tinkoff.kora.database.jdbc.EntityJdbc

@EntityJdbc
@Table("vets")
data class Vet(
    @field:Column("id") @field:Id val id: Long,
    @field:Column("name") val name: String,
    @field:Column("surname") val surname: String,
)

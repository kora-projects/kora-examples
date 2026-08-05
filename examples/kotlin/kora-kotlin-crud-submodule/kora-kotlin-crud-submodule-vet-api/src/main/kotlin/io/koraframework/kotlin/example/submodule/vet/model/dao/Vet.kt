package io.koraframework.kotlin.example.submodule.vet.model.dao

import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Table
import io.koraframework.database.jdbc.annotation.EntityJdbc

@EntityJdbc
@Table("vets")
data class Vet(
    @field:Column("id") @field:Id val id: Long,
    @field:Column("name") val name: String,
    @field:Column("surname") val surname: String,
)

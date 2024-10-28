package ru.tinkoff.kora.kotlin.example.crud.model

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Embedded
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table

@Table("pets")
data class Pet(
    @field:Column("id") @field:Id val id: Long,
    @field:Column("name") val name: String,
    @field:Column("status") val status: Status,
    @field:Column("category_id") val categoryId: Long
) {
    enum class Status(val code: Int) {
        AVAILABLE(0),
        PENDING(10),
        SOLD(20)
    }
}

@Table("categories")
data class PetCategory(
    @field:Id val id: Long,
    val name: String
)

data class PetWithCategory(
    @field:Column("id") val id: Long,
    @field:Column("name") val name: String,
    @field:Column("status") val status: Pet.Status,
    @field:Embedded("category_") val category: PetCategory
) {

    fun getPet(): Pet = Pet(id, name, status, category.id)
}

package io.koraframework.kotlin.example.petclinic.model

import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Embedded
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Table
import io.koraframework.database.jdbc.annotation.EntityJdbc
import java.time.LocalDate

@EntityJdbc
@Table("owners")
data class Owner(
    @field:Id val id: Long,
    @field:Column("first_name") val firstName: String,
    @field:Column("last_name") val lastName: String,
    val address: String,
    val city: String,
    val telephone: String,
)

@EntityJdbc
@Table("pet_types")
data class PetType(@field:Id val id: Long, val name: String)

@EntityJdbc
@Table("pets")
data class Pet(
    @field:Id val id: Long,
    @field:Column("owner_id") val ownerId: Long,
    @field:Column("type_id") val typeId: Long,
    val name: String,
    @field:Column("birth_date") val birthDate: LocalDate,
)

@EntityJdbc
@Table("visits")
data class Visit(
    @field:Id val id: Long,
    @field:Column("pet_id") val petId: Long,
    @field:Column("visit_date") val visitDate: LocalDate,
    val description: String,
)

@EntityJdbc
@Table("vets")
data class Vet(
    @field:Id val id: Long,
    @field:Column("first_name") val firstName: String,
    @field:Column("last_name") val lastName: String,
)

@EntityJdbc
@Table("specialties")
// TODO(Kora next version): make joined fields non-null after the @Embedded List<T> LEFT JOIN mapper fix is released.
data class Specialty(@field:Id val id: Long?, val name: String?)

@EntityJdbc
data class VetWithSpecialties(
    @field:Embedded("v_") val vet: Vet,
    @field:Embedded("s_") val specialties: List<Specialty>,
)

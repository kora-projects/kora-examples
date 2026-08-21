package io.koraframework.kotlin.example.petclinic.model

import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.NotBlank
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size
import io.koraframework.validation.common.annotation.Valid
import java.time.LocalDate

@Json
@Valid
data class OwnerRequest(
    @field:NotBlank @field:Size(max = 30) val firstName: String,
    @field:NotBlank @field:Size(max = 30) val lastName: String,
    @field:NotBlank @field:Size(max = 255) val address: String,
    @field:NotBlank @field:Size(max = 80) val city: String,
    @field:Pattern("^[0-9]{1,20}$") val telephone: String,
)

@Json
@Valid
data class PetRequest(
    @field:NotBlank @field:Size(max = 30) val name: String,
    val birthDate: LocalDate,
    val typeId: Long,
)

@Json
@Valid
data class VisitRequest(
    val visitDate: LocalDate,
    @field:NotBlank @field:Size(max = 255) val description: String,
)

@Json
data class VisitView(val id: Long, val visitDate: LocalDate, val description: String)

@Json
data class PetTypeView(val id: Long, val name: String)

@Json
data class PetView(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val type: PetTypeView,
    val visits: List<VisitView>,
)

@Json
data class OwnerView(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val address: String,
    val city: String,
    val telephone: String,
    val pets: List<PetView>,
)

@Json
data class VetView(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val specialties: List<String>,
)

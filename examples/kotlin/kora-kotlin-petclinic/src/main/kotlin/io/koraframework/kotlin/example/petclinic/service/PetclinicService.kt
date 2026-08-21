package io.koraframework.kotlin.example.petclinic.service

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.JdbcExecutor
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.kotlin.example.petclinic.model.Owner
import io.koraframework.kotlin.example.petclinic.model.OwnerRequest
import io.koraframework.kotlin.example.petclinic.model.OwnerView
import io.koraframework.kotlin.example.petclinic.model.Pet
import io.koraframework.kotlin.example.petclinic.model.PetRequest
import io.koraframework.kotlin.example.petclinic.model.PetType
import io.koraframework.kotlin.example.petclinic.model.PetTypeView
import io.koraframework.kotlin.example.petclinic.model.PetView
import io.koraframework.kotlin.example.petclinic.model.VetView
import io.koraframework.kotlin.example.petclinic.model.Visit
import io.koraframework.kotlin.example.petclinic.model.VisitRequest
import io.koraframework.kotlin.example.petclinic.model.VisitView
import io.koraframework.kotlin.example.petclinic.repository.OwnerRepository
import io.koraframework.kotlin.example.petclinic.repository.PetRepository
import io.koraframework.kotlin.example.petclinic.repository.ReferenceRepository
import io.koraframework.kotlin.example.petclinic.repository.VisitRepository

@Component
class PetclinicService(
    private val owners: OwnerRepository,
    private val pets: PetRepository,
    private val visits: VisitRepository,
    private val references: ReferenceRepository,
) {
    fun findOwners(lastName: String?): List<OwnerView> {
        val pattern = if (lastName.isNullOrBlank()) "%" else "%${lastName.trim()}%"
        return owners.findByLastName(pattern).map(::toOwnerView)
    }

    fun findOwner(id: Long): OwnerView = toOwnerView(requireOwner(id))

    fun createOwner(request: OwnerRequest): OwnerView = owners.executor().inTx(JdbcExecutor.SqlSupplier {
        val owner = Owner(0, request.firstName, request.lastName, request.address, request.city, request.telephone)
        toOwnerView(owner.copy(id = owners.insert(owner)))
    })

    fun updateOwner(id: Long, request: OwnerRequest): OwnerView {
        val owner = Owner(id, request.firstName, request.lastName, request.address, request.city, request.telephone)
        if (owners.update(owner).value() == 0L) throw notFound("Owner", id)
        return toOwnerView(owner)
    }

    fun addPet(ownerId: Long, request: PetRequest): PetView = owners.executor().inTx(JdbcExecutor.SqlSupplier {
        requireOwner(ownerId)
        requirePetType(request.typeId)
        val pet = Pet(0, ownerId, request.typeId, request.name, request.birthDate)
        toPetView(pet.copy(id = pets.insert(pet)))
    })

    fun updatePet(ownerId: Long, petId: Long, request: PetRequest): PetView {
        requireOwner(ownerId)
        requirePetType(request.typeId)
        val current = requirePet(petId)
        if (current.ownerId != ownerId) throw notFound("Pet", petId)
        val pet = Pet(petId, ownerId, request.typeId, request.name, request.birthDate)
        pets.update(pet)
        return toPetView(pet)
    }

    fun addVisit(ownerId: Long, petId: Long, request: VisitRequest): VisitView =
        owners.executor().inTx(JdbcExecutor.SqlSupplier {
            requireOwner(ownerId)
            val pet = requirePet(petId)
            if (pet.ownerId != ownerId) throw notFound("Pet", petId)
            val visit = Visit(0, petId, request.visitDate, request.description)
            val saved = visit.copy(id = visits.insert(visit))
            VisitView(saved.id, saved.visitDate, saved.description)
        })

    fun findPetTypes(): List<PetTypeView> = references.findPetTypes().map { PetTypeView(it.id, it.name) }

    fun findVets(): List<VetView> = references.findVets().map { result ->
        VetView(
            result.vet.id,
            result.vet.firstName,
            result.vet.lastName,
            // TODO(Kora next version): replace mapNotNull with map after joined Specialty fields become non-null.
            result.specialties.mapNotNull { it.name },
        )
    }

    private fun toOwnerView(owner: Owner): OwnerView = OwnerView(
        owner.id, owner.firstName, owner.lastName, owner.address, owner.city, owner.telephone,
        pets.findByOwnerId(owner.id).map(::toPetView),
    )

    private fun toPetView(pet: Pet): PetView {
        val type = requirePetType(pet.typeId)
        return PetView(
            pet.id, pet.name, pet.birthDate, PetTypeView(type.id, type.name),
            visits.findByPetId(pet.id).map { VisitView(it.id, it.visitDate, it.description) },
        )
    }

    private fun requireOwner(id: Long): Owner = owners.findById(id) ?: throw notFound("Owner", id)
    private fun requirePet(id: Long): Pet = pets.findById(id) ?: throw notFound("Pet", id)
    private fun requirePetType(id: Long): PetType = references.findPetType(id) ?: throw notFound("Pet type", id)

    private fun notFound(type: String, id: Long): HttpServerResponseException =
        HttpServerResponseException.of(404, "$type $id not found")
}

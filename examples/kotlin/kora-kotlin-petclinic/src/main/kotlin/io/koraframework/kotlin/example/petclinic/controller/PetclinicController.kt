package io.koraframework.kotlin.example.petclinic.controller

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json
import io.koraframework.kotlin.example.petclinic.model.OwnerRequest
import io.koraframework.kotlin.example.petclinic.model.OwnerView
import io.koraframework.kotlin.example.petclinic.model.PetRequest
import io.koraframework.kotlin.example.petclinic.model.PetTypeView
import io.koraframework.kotlin.example.petclinic.model.PetView
import io.koraframework.kotlin.example.petclinic.model.VetView
import io.koraframework.kotlin.example.petclinic.model.VisitRequest
import io.koraframework.kotlin.example.petclinic.model.VisitView
import io.koraframework.kotlin.example.petclinic.service.PetclinicService
import io.koraframework.validation.common.annotation.Valid
import io.koraframework.validation.common.annotation.Validate

@Component
@HttpController
open class PetclinicController(private val service: PetclinicService) {
    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/owners")
    fun owners(@Query lastName: String?): List<OwnerView> = service.findOwners(lastName)

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/owners/{ownerId}")
    fun owner(@Path ownerId: Long): OwnerView = service.findOwner(ownerId)

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners")
    open fun createOwner(@Json @Valid request: OwnerRequest): HttpResponseEntity<OwnerView> =
        HttpResponseEntity.of(201, service.createOwner(request))

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.PUT, path = "/api/owners/{ownerId}")
    open fun updateOwner(@Path ownerId: Long, @Json @Valid request: OwnerRequest): OwnerView =
        service.updateOwner(ownerId, request)

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners/{ownerId}/pets")
    open fun addPet(@Path ownerId: Long, @Json @Valid request: PetRequest): HttpResponseEntity<PetView> =
        HttpResponseEntity.of(201, service.addPet(ownerId, request))

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.PUT, path = "/api/owners/{ownerId}/pets/{petId}")
    open fun updatePet(@Path ownerId: Long, @Path petId: Long, @Json @Valid request: PetRequest): PetView =
        service.updatePet(ownerId, petId, request)

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners/{ownerId}/pets/{petId}/visits")
    open fun addVisit(
        @Path ownerId: Long,
        @Path petId: Long,
        @Json @Valid request: VisitRequest,
    ): HttpResponseEntity<VisitView> = HttpResponseEntity.of(201, service.addVisit(ownerId, petId, request))

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/pet-types")
    fun petTypes(): List<PetTypeView> = service.findPetTypes()

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/vets")
    fun vets(): List<VetView> = service.findVets()
}

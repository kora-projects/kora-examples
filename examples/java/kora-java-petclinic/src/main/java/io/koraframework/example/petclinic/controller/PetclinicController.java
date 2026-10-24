package io.koraframework.example.petclinic.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.example.petclinic.model.dto.OwnerRequest;
import io.koraframework.example.petclinic.model.dto.OwnerView;
import io.koraframework.example.petclinic.model.dto.PetRequest;
import io.koraframework.example.petclinic.model.dto.PetTypeView;
import io.koraframework.example.petclinic.model.dto.PetView;
import io.koraframework.example.petclinic.model.dto.VetView;
import io.koraframework.example.petclinic.model.dto.VisitRequest;
import io.koraframework.example.petclinic.model.dto.VisitView;
import io.koraframework.example.petclinic.service.PetclinicService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.validation.common.annotation.Valid;
import io.koraframework.validation.common.annotation.Validate;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Component
@HttpController
public class PetclinicController {

    private final PetclinicService service;

    public PetclinicController(PetclinicService service) {
        this.service = service;
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/owners")
    public List<OwnerView> owners(@Nullable @Query String lastName) {
        return service.findOwners(lastName);
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/owners/{ownerId}")
    public OwnerView owner(@Path long ownerId) {
        return service.findOwner(ownerId);
    }

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners")
    public HttpResponseEntity<OwnerView> createOwner(@Json @Valid OwnerRequest request) {
        return HttpResponseEntity.of(201, service.createOwner(request));
    }

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.PUT, path = "/api/owners/{ownerId}")
    public OwnerView updateOwner(@Path long ownerId, @Json @Valid OwnerRequest request) {
        return service.updateOwner(ownerId, request);
    }

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners/{ownerId}/pets")
    public HttpResponseEntity<PetView> addPet(@Path long ownerId, @Json @Valid PetRequest request) {
        return HttpResponseEntity.of(201, service.addPet(ownerId, request));
    }

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.PUT, path = "/api/owners/{ownerId}/pets/{petId}")
    public PetView updatePet(@Path long ownerId, @Path long petId, @Json @Valid PetRequest request) {
        return service.updatePet(ownerId, petId, request);
    }

    @Json
    @Validate
    @HttpRoute(method = HttpMethod.POST, path = "/api/owners/{ownerId}/pets/{petId}/visits")
    public HttpResponseEntity<VisitView> addVisit(@Path long ownerId,
                                                  @Path long petId,
                                                  @Json @Valid VisitRequest request) {
        return HttpResponseEntity.of(201, service.addVisit(ownerId, petId, request));
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/pet-types")
    public List<PetTypeView> petTypes() {
        return service.findPetTypes();
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/api/vets")
    public List<VetView> vets() {
        return service.findVets();
    }
}

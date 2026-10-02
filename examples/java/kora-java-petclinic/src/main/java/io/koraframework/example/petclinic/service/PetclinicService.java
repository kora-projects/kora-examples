package io.koraframework.example.petclinic.service;

import io.koraframework.common.annotation.Component;
import io.koraframework.example.petclinic.model.dao.Owner;
import io.koraframework.example.petclinic.model.dao.Pet;
import io.koraframework.example.petclinic.model.dao.PetType;
import io.koraframework.example.petclinic.model.dao.Specialty;
import io.koraframework.example.petclinic.model.dao.Visit;
import io.koraframework.example.petclinic.model.dto.OwnerRequest;
import io.koraframework.example.petclinic.model.dto.OwnerView;
import io.koraframework.example.petclinic.model.dto.PetRequest;
import io.koraframework.example.petclinic.model.dto.PetTypeView;
import io.koraframework.example.petclinic.model.dto.PetView;
import io.koraframework.example.petclinic.model.dto.VetView;
import io.koraframework.example.petclinic.model.dto.VisitRequest;
import io.koraframework.example.petclinic.model.dto.VisitView;
import io.koraframework.example.petclinic.repository.OwnerRepository;
import io.koraframework.example.petclinic.repository.PetRepository;
import io.koraframework.example.petclinic.repository.ReferenceRepository;
import io.koraframework.example.petclinic.repository.VisitRepository;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import java.util.List;

@Component
public final class PetclinicService {

    private final OwnerRepository owners;
    private final PetRepository pets;
    private final VisitRepository visits;
    private final ReferenceRepository references;

    public PetclinicService(OwnerRepository owners,
                            PetRepository pets,
                            VisitRepository visits,
                            ReferenceRepository references) {
        this.owners = owners;
        this.pets = pets;
        this.visits = visits;
        this.references = references;
    }

    public List<OwnerView> findOwners(String lastName) {
        var pattern = lastName == null || lastName.isBlank()
                ? "%"
                : "%" + lastName.trim() + "%";
        return owners.findByLastName(pattern).stream().map(this::toOwnerView).toList();
    }

    public OwnerView findOwner(long id) {
        return toOwnerView(requireOwner(id));
    }

    public OwnerView createOwner(OwnerRequest request) {
        return owners.executor().inTx(() -> {
            var owner = new Owner(0, request.firstName(), request.lastName(), request.address(), request.city(),
                    request.telephone());
            return toOwnerView(ownerWithId(owner, owners.insert(owner)));
        });
    }

    public OwnerView updateOwner(long id, OwnerRequest request) {
        var owner = new Owner(id, request.firstName(), request.lastName(), request.address(), request.city(),
                request.telephone());
        if (owners.update(owner).value() == 0) {
            throw notFound("Owner", id);
        }
        return toOwnerView(owner);
    }

    public PetView addPet(long ownerId, PetRequest request) {
        return owners.executor().inTx(() -> {
            requireOwner(ownerId);
            requirePetType(request.typeId());
            var pet = new Pet(0, ownerId, request.typeId(), request.name(), request.birthDate());
            return toPetView(petWithId(pet, pets.insert(pet)));
        });
    }

    public PetView updatePet(long ownerId, long petId, PetRequest request) {
        requireOwner(ownerId);
        requirePetType(request.typeId());
        var current = requirePet(petId);
        if (current.ownerId() != ownerId) {
            throw notFound("Pet", petId);
        }
        var pet = new Pet(petId, ownerId, request.typeId(), request.name(), request.birthDate());
        pets.update(pet);
        return toPetView(pet);
    }

    public VisitView addVisit(long ownerId, long petId, VisitRequest request) {
        return owners.executor().inTx(() -> {
            requireOwner(ownerId);
            var pet = requirePet(petId);
            if (pet.ownerId() != ownerId) {
                throw notFound("Pet", petId);
            }
            var visit = new Visit(0, petId, request.visitDate(), request.description());
            var saved = visitWithId(visit, visits.insert(visit));
            return new VisitView(saved.id(), saved.visitDate(), saved.description());
        });
    }

    public List<PetTypeView> findPetTypes() {
        return references.findPetTypes().stream().map(t -> new PetTypeView(t.id(), t.name())).toList();
    }

    public List<VetView> findVets() {
        return references.findVets().stream()
                .map(v -> new VetView(v.vet().id(), v.vet().firstName(), v.vet().lastName(),
                        v.specialties().stream().map(Specialty::name).toList()))
                .toList();
    }

    private OwnerView toOwnerView(Owner owner) {
        var ownerPets = pets.findByOwnerId(owner.id()).stream().map(this::toPetView).toList();
        return new OwnerView(owner.id(), owner.firstName(), owner.lastName(), owner.address(), owner.city(), owner.telephone(),
                ownerPets);
    }

    private PetView toPetView(Pet pet) {
        var type = requirePetType(pet.typeId());
        var petVisits = visits.findByPetId(pet.id()).stream()
                .map(v -> new VisitView(v.id(), v.visitDate(), v.description()))
                .toList();
        return new PetView(pet.id(), pet.name(), pet.birthDate(), new PetTypeView(type.id(), type.name()), petVisits);
    }

    private Owner requireOwner(long id) {
        return owners.findById(id).orElseThrow(() -> notFound("Owner", id));
    }

    private Pet requirePet(long id) {
        return pets.findById(id).orElseThrow(() -> notFound("Pet", id));
    }

    private PetType requirePetType(long id) {
        return references.findPetType(id).orElseThrow(() -> notFound("Pet type", id));
    }

    private static Owner ownerWithId(Owner owner, long id) {
        return new Owner(id, owner.firstName(), owner.lastName(), owner.address(), owner.city(), owner.telephone());
    }

    private static Pet petWithId(Pet pet, long id) {
        return new Pet(id, pet.ownerId(), pet.typeId(), pet.name(), pet.birthDate());
    }

    private static Visit visitWithId(Visit visit, long id) {
        return new Visit(id, visit.petId(), visit.visitDate(), visit.description());
    }

    private static HttpServerResponseException notFound(String type, long id) {
        return HttpServerResponseException.of(404, type + " " + id + " not found");
    }
}

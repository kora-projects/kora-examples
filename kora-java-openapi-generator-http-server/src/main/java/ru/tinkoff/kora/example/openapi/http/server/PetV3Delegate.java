package ru.tinkoff.kora.example.openapi.http.server;

import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.openapi.petV3.api.PetApiDelegate;
import ru.tinkoff.kora.example.openapi.petV3.api.PetApiResponses;
import ru.tinkoff.kora.example.openapi.petV3.model.Message;
import ru.tinkoff.kora.example.openapi.petV3.model.Pet;
import ru.tinkoff.kora.validation.common.Validator;

@Component
public final class PetV3Delegate implements PetApiDelegate {

    private final Map<Long, Pet> petMap = new ConcurrentHashMap<>();

    private final Validator<Pet> petValidator;

    public PetV3Delegate(Validator<Pet> petValidator) {
        this.petValidator = petValidator;
    }

    @Override
    public Mono<PetApiResponses.AddPetApiResponse> addPet(Pet body) {
        var violations = petValidator.validate(body);
        if (!violations.isEmpty()) {
            return Mono.just(new PetApiResponses.AddPetApiResponse.AddPet405ApiResponse());
        }

        petMap.put(body.id(), body);
        return Mono.just(new PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(body));
    }

    @Override
    public Mono<PetApiResponses.DeletePetApiResponse> deletePet(long petId, @Nullable String apiKey) {
        petMap.remove(petId);
        return Mono.just(new PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(new Message("OK")));
    }

    @Override
    public Mono<PetApiResponses.FindPetsByStatusApiResponse> findPetsByStatus(@Nullable String status) {
        if (status == null) {
            return Mono.just(new PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus400ApiResponse());
        }

        final Pet.StatusEnum statusEnum = Pet.StatusEnum.valueOf(status);
        final List<Pet> pets = petMap.values().stream()
                .filter(p -> statusEnum.equals(p.status()))
                .toList();

        return Mono.just(new PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus200ApiResponse(pets));
    }

    @Override
    public Mono<PetApiResponses.FindPetsByTagsApiResponse> findPetsByTags(List<String> tags) {
        final Set<String> petTags = new HashSet<>(tags);
        final List<Pet> pets = petMap.values().stream()
                .filter(p -> p.tags().stream().allMatch(tag -> petTags.contains(tag.name())))
                .toList();

        if (pets.isEmpty()) {
            return Mono.just(new PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags400ApiResponse());
        } else {
            return Mono.just(new PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags200ApiResponse(pets));
        }
    }

    @Override
    public Mono<PetApiResponses.GetPetByIdApiResponse> getPetById(long petId) {
        if (petId < 0) {
            return Mono.just(new PetApiResponses.GetPetByIdApiResponse.GetPetById400ApiResponse());
        }

        final Pet pet = petMap.get(petId);
        if (pet == null) {
            return Mono.just(new PetApiResponses.GetPetByIdApiResponse.GetPetById404ApiResponse());
        } else {
            return Mono.just(new PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse(pet));
        }
    }

    @Override
    public Mono<PetApiResponses.UpdatePetApiResponse> updatePet(Pet body) {
        if (!petMap.containsKey(body.id())) {
            return Mono.just(new PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse());
        }

        var violations = petValidator.validate(body);
        if (!violations.isEmpty()) {
            return Mono.just(new PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse());
        }

        petMap.put(body.id(), body);
        return Mono.just(new PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(body));
    }

    @Override
    public Mono<PetApiResponses.UpdatePetWithFormApiResponse>
            updatePetWithForm(long petId, @Nullable String name, @Nullable String status) {
        final Pet pet = petMap.get(petId);
        if (pet == null) {
            return Mono.just(new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm404ApiResponse());
        }

        Pet updated = pet;
        if (name != null) {
            updated = pet.withName(name);
        }
        if (status != null) {
            updated = pet.withStatus(Pet.StatusEnum.valueOf(status));
        }

        petMap.put(updated.id(), updated);
        return Mono.just(new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(new Message("OK")));
    }
}

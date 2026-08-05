package io.koraframework.example.openapi.http.server;

import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.openapi.petV3.api.PetApiDelegate;
import io.koraframework.example.openapi.petV3.api.PetApiResponses;
import io.koraframework.example.openapi.petV3.model.Message;
import io.koraframework.example.openapi.petV3.model.Pet;

@Component
public final class PetV3Delegate implements PetApiDelegate {

    private final Map<Long, Pet> petMap = new ConcurrentHashMap<>();

    @Override
    public PetApiResponses.AddPetApiResponse addPet(Pet body) {
        petMap.put(body.id(), body);
        return new PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(body);
    }

    @Override
    public PetApiResponses.DeletePetApiResponse deletePet(long petId, @Nullable String apiKey) {
        petMap.remove(petId);
        return new PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(new Message("OK"));
    }

    @Override
    public PetApiResponses.FindPetsByStatusApiResponse findPetsByStatus(@Nullable String status) {
        if (status == null) {
            return new PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus400ApiResponse();
        }

        final Pet.StatusEnum statusEnum = Pet.StatusEnum.valueOf(status);
        final List<Pet> pets = petMap.values().stream()
                .filter(p -> statusEnum.equals(p.status()))
                .toList();

        return new PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus200ApiResponse(pets);
    }

    @Override
    public PetApiResponses.FindPetsByTagsApiResponse findPetsByTags(List<String> tags) {
        final Set<String> petTags = new HashSet<>(tags);
        final List<Pet> pets = petMap.values().stream()
                .filter(p -> p.tags() != null)
                .filter(p -> p.tags().stream().allMatch(tag -> petTags.contains(tag.name())))
                .toList();

        if (pets.isEmpty()) {
            return new PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags400ApiResponse();
        } else {
            return new PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags200ApiResponse(pets);
        }
    }

    @Override
    public PetApiResponses.GetPetByIdApiResponse getPetById(long petId) {
        if (petId < 0) {
            return new PetApiResponses.GetPetByIdApiResponse.GetPetById400ApiResponse();
        }

        final Pet pet = petMap.get(petId);
        if (pet == null) {
            return new PetApiResponses.GetPetByIdApiResponse.GetPetById404ApiResponse();
        } else {
            return new PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse(pet);
        }
    }

    @Override
    public PetApiResponses.UpdatePetApiResponse updatePet(Pet body) {
        if (!petMap.containsKey(body.id())) {
            return new PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse();
        }

        petMap.put(body.id(), body);
        return new PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(body);
    }

    @Override
    public PetApiResponses.UpdatePetWithFormApiResponse
            updatePetWithForm(long petId, @Nullable String name, @Nullable String status) {
        final Pet pet = petMap.get(petId);
        if (pet == null) {
            return new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm404ApiResponse();
        }

        Pet updated = pet;
        if (name != null) {
            updated = pet.withName(name);
        }
        if (status != null) {
            updated = pet.withStatus(Pet.StatusEnum.valueOf(status));
        }

        petMap.put(updated.id(), updated);
        return new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(new Message("OK"));
    }
}

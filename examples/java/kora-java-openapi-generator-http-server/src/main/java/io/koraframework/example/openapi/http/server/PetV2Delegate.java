package io.koraframework.example.openapi.http.server;

import org.jspecify.annotations.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.openapi.petV2.api.PetApiController;
import io.koraframework.example.openapi.petV2.api.PetApiDelegate;
import io.koraframework.example.openapi.petV2.api.PetApiResponses;
import io.koraframework.example.openapi.petV2.model.Message;
import io.koraframework.example.openapi.petV2.model.Pet;

@Component
public final class PetV2Delegate implements PetApiDelegate {

    private final Map<Long, Pet> petMap = new ConcurrentHashMap<>();

    @Override
    public PetApiResponses.AddPetApiResponse addPet(Pet body) {
        petMap.put(body.id(), body);
        return new PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(new Message("OK"));
    }

    @Override
    public PetApiResponses.DeletePetApiResponse deletePet(long petId, @Nullable String apiKey) {
        petMap.remove(petId);
        return new PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(new Message("OK"));
    }

    @Override
    public PetApiResponses.FindPetsByStatusApiResponse findPetsByStatus(List<String> status) {
        final Set<Pet.StatusEnum> petStatuses = status.stream()
                .map(Pet.StatusEnum::fromValue)
                .collect(Collectors.toSet());

        final List<Pet> pets = petMap.values().stream()
                .filter(p -> petStatuses.contains(p.status()))
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
        return new PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(new Message("OK"));
    }

    @Override
    public PetApiResponses.UpdatePetWithFormApiResponse updatePetWithForm(long petId,
                                                                          PetApiController.UpdatePetWithFormFormParam form) {
        final Pet pet = petMap.get(petId);
        if (pet == null) {
            return new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm404ApiResponse();
        }

        final Pet updated = pet
                .withName(form.name())
                .withStatus(form.status() == null ? null : Pet.StatusEnum.fromValue(form.status()));
        petMap.put(updated.id(), updated);

        return new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(new Message("OK"));
    }
}

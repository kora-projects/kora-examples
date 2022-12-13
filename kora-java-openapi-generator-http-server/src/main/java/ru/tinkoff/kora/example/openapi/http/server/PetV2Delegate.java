package ru.tinkoff.kora.example.openapi.http.server;

import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApiController;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApiDelegate;
import ru.tinkoff.kora.example.openapi.petV2.api.PetApiResponses;
import ru.tinkoff.kora.example.openapi.petV2.model.Message;
import ru.tinkoff.kora.example.openapi.petV2.model.Pet;
import ru.tinkoff.kora.validation.common.Validator;

@Component
public final class PetV2Delegate implements PetApiDelegate {

    private final Map<Long, Pet> petMap = new ConcurrentHashMap<>();

    private final Validator<Pet> petValidator;

    public PetV2Delegate(Validator<Pet> petValidator) {
        this.petValidator = petValidator;
    }

    @Override
    public PetApiResponses.AddPetApiResponse addPet(Pet body) {
        var violations = petValidator.validate(body);
        if (!violations.isEmpty()) {
            return new PetApiResponses.AddPetApiResponse.AddPet405ApiResponse();
        }

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
                .map(Pet.StatusEnum::valueOf)
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

        var violations = petValidator.validate(body);
        if (!violations.isEmpty()) {
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
                .withStatus(Pet.StatusEnum.valueOf(form.status()));
        petMap.put(updated.id(), updated);

        return new PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(new Message("OK"));
    }
}

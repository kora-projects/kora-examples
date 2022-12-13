package ru.tinkoff.kora.example.crud.controller;

import static ru.tinkoff.kora.example.crud.openapi.http.server.api.PetApiResponses.*;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.crud.model.mapper.PetMapper;
import ru.tinkoff.kora.example.crud.openapi.http.server.api.PetApiDelegate;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.*;
import ru.tinkoff.kora.example.crud.service.PetService;

@Component
public final class PetDelegate implements PetApiDelegate {

    private final PetMapper petMapper;
    private final PetService petService;

    public PetDelegate(PetMapper petMapper, PetService petService) {
        this.petMapper = petMapper;
        this.petService = petService;
    }

    @Override
    public GetPetByIdApiResponse getPetById(long petId) {
        if (petId < 0) {
            return new GetPetByIdApiResponse.GetPetById400ApiResponse(malformedId(petId));
        }

        var pet = petService.findByID(petId);
        if (pet.isPresent()) {
            var body = petMapper.asDTO(pet.get());
            return new GetPetByIdApiResponse.GetPetById200ApiResponse(body);
        } else {
            return new GetPetByIdApiResponse.GetPetById404ApiResponse(notFound(petId));
        }
    }

    @Override
    public AddPetApiResponse addPet(PetCreateTO petCreateTO) {
        var pet = petService.add(petCreateTO);
        var body = petMapper.asDTO(pet);
        return new AddPetApiResponse.AddPet200ApiResponse(body);
    }

    @Override
    public UpdatePetApiResponse updatePet(long petId, PetUpdateTO petUpdateTO) {
        if (petId < 0) {
            return new UpdatePetApiResponse.UpdatePet400ApiResponse(malformedId(petId));
        }

        var updated = petService.update(petId, petUpdateTO);
        if (updated.isPresent()) {
            var body = petMapper.asDTO(updated.get());
            return new UpdatePetApiResponse.UpdatePet200ApiResponse(body);
        } else {
            return new UpdatePetApiResponse.UpdatePet404ApiResponse(notFound(petId));
        }
    }

    @Override
    public DeletePetApiResponse deletePet(long petId) {
        if (petId < 0) {
            return new DeletePetApiResponse.DeletePet400ApiResponse(malformedId(petId));
        }

        if (petService.delete(petId)) {
            return new DeletePetApiResponse.DeletePet200ApiResponse(new MessageTO("Successfully deleted pet with ID: " + petId));
        } else {
            return new DeletePetApiResponse.DeletePet404ApiResponse(notFound(petId));
        }
    }

    private static MessageTO notFound(long petId) {
        return new MessageTO("Pet not found for ID: " + petId);
    }

    private static MessageTO malformedId(long petId) {
        return new MessageTO("Pet malformed ID: " + petId);
    }
}

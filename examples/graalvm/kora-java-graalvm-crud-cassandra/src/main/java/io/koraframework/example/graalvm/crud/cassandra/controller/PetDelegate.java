package io.koraframework.example.graalvm.crud.cassandra.controller;

import static io.koraframework.example.graalvm.crud.openapi.server.api.PetApiResponses.*;

import io.koraframework.common.annotation.Component;
import io.koraframework.example.graalvm.crud.cassandra.model.mapper.PetMapper;
import io.koraframework.example.graalvm.crud.cassandra.service.PetService;
import io.koraframework.example.graalvm.crud.openapi.server.api.PetApiDelegate;
import io.koraframework.example.graalvm.crud.openapi.server.model.MessageTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetCreateTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetUpdateTO;

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
        if (pet == null) {
            return new GetPetByIdApiResponse.GetPetById404ApiResponse(notFound(petId));
        }

        var body = petMapper.asDTO(pet);
        return new GetPetByIdApiResponse.GetPetById200ApiResponse(body);
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
        if (updated == null) {
            return new UpdatePetApiResponse.UpdatePet404ApiResponse(notFound(petId));
        }

        var body = petMapper.asDTO(updated);
        return new UpdatePetApiResponse.UpdatePet200ApiResponse(body);
    }

    @Override
    public DeletePetApiResponse deletePet(long petId) {
        if (petId < 0) {
            return new DeletePetApiResponse.DeletePet400ApiResponse(malformedId(petId));
        }

        if (petService.delete(petId)) {
            return new DeletePetApiResponse.DeletePet200ApiResponse(
                    new MessageTO("Successfully deleted pet with ID: " + petId));
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

package ru.tinkoff.kora.example.graalvm.crud.r2dbc.controller;

import static ru.tinkoff.kora.example.graalvm.crud.openapi.server.api.PetApiResponses.*;

import reactor.core.publisher.Mono;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.api.PetApiDelegate;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.MessageTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetCreateTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.mapper.PetMapper;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.service.PetService;

@Component
public final class PetDelegate implements PetApiDelegate {

    private final PetMapper petMapper;
    private final PetService petService;

    public PetDelegate(PetMapper petMapper, PetService petService) {
        this.petMapper = petMapper;
        this.petService = petService;
    }

    @Override
    public Mono<GetPetByIdApiResponse> getPetById(long petId) {
        if (petId < 0) {
            return Mono.just(new GetPetByIdApiResponse.GetPetById400ApiResponse(malformedId(petId)));
        }

        return petService.findByID(petId)
                .map(pet -> {
                    var body = petMapper.asDTO(pet);
                    return ((GetPetByIdApiResponse) new GetPetByIdApiResponse.GetPetById200ApiResponse(body));
                })
                .switchIfEmpty(Mono.fromSupplier(() -> new GetPetByIdApiResponse.GetPetById404ApiResponse(notFound(petId))));
    }

    @Override
    public Mono<AddPetApiResponse> addPet(PetCreateTO petCreateTO) {
        return petService.add(petCreateTO)
                .map(pet -> {
                    var body = petMapper.asDTO(pet);
                    return new AddPetApiResponse.AddPet200ApiResponse(body);
                });
    }

    @Override
    public Mono<UpdatePetApiResponse> updatePet(long petId, PetUpdateTO petUpdateTO) {
        if (petId < 0) {
            return Mono.just(new UpdatePetApiResponse.UpdatePet400ApiResponse(malformedId(petId)));
        }

        return petService.update(petId, petUpdateTO)
                .map(pet -> {
                    var body = petMapper.asDTO(pet);
                    return ((UpdatePetApiResponse) new UpdatePetApiResponse.UpdatePet200ApiResponse(body));
                })
                .switchIfEmpty(Mono.fromSupplier(() -> new UpdatePetApiResponse.UpdatePet404ApiResponse(notFound(petId))));
    }

    @Override
    public Mono<DeletePetApiResponse> deletePet(long petId) {
        if (petId < 0) {
            return Mono.just(new DeletePetApiResponse.DeletePet400ApiResponse(malformedId(petId)));
        }

        return petService.delete(petId)
                .map(isDeleted -> (isDeleted)
                        ? new DeletePetApiResponse.DeletePet200ApiResponse(
                                new MessageTO("Successfully deleted pet with ID: " + petId))
                        : new DeletePetApiResponse.DeletePet404ApiResponse(notFound(petId)));
    }

    private static MessageTO notFound(long petId) {
        return new MessageTO("Pet not found for ID: " + petId);
    }

    private static MessageTO malformedId(long petId) {
        return new MessageTO("Pet malformed ID: " + petId);
    }
}

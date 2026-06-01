package ru.tinkoff.kora.example.submodule.app.controller;

import static ru.tinkoff.kora.example.submodule.openapi.http.server.api.VetApiResponses.*;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.submodule.app.model.VetMapper;
import ru.tinkoff.kora.example.submodule.openapi.http.server.api.VetApiDelegate;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.MessageTO;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.VetCreateTO;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.VetUpdateTO;
import ru.tinkoff.kora.example.submodule.vet.service.VetService;

@Component
public final class VetDelegate implements VetApiDelegate {

    private final VetMapper vetMapper;
    private final VetService vetService;

    public VetDelegate(VetMapper vetMapper, VetService vetService) {
        this.vetMapper = vetMapper;
        this.vetService = vetService;
    }

    @Override
    public ListVetsApiResponse listVets() {
        var vets = vetService.findAll();
        var vetTOs = vets.stream()
                .map(vetMapper::asDTO)
                .toList();

        return new ListVetsApiResponse.ListVets200ApiResponse(vetTOs);
    }

    @Override
    public GetVetByIdApiResponse getVetById(long vetId) {
        if (vetId < 0) {
            return new GetVetByIdApiResponse.GetVetById400ApiResponse(malformedId(vetId));
        }

        var vet = vetService.findByID(vetId);
        if (vet.isPresent()) {
            var body = vetMapper.asDTO(vet.get());
            return new GetVetByIdApiResponse.GetVetById200ApiResponse(body);
        } else {
            return new GetVetByIdApiResponse.GetVetById404ApiResponse(notFound(vetId));
        }
    }

    @Override
    public AddVetApiResponse addVet(VetCreateTO vetCreateTO) {
        var vet = vetService.add(vetCreateTO.firstName(), vetCreateTO.lastName());
        var body = vetMapper.asDTO(vet);
        return new AddVetApiResponse.AddVet200ApiResponse(body);
    }

    @Override
    public UpdateVetApiResponse updateVet(long vetId, VetUpdateTO vetUpdateTO) {
        if (vetId < 0) {
            return new UpdateVetApiResponse.UpdateVet400ApiResponse(malformedId(vetId));
        }

        var updated = vetService.update(vetId, vetUpdateTO.firstName(), vetUpdateTO.lastName());
        if (updated.isPresent()) {
            var body = vetMapper.asDTO(updated.get());
            return new UpdateVetApiResponse.UpdateVet200ApiResponse(body);
        } else {
            return new UpdateVetApiResponse.UpdateVet404ApiResponse(notFound(vetId));
        }
    }

    @Override
    public DeleteVetApiResponse deleteVet(long vetId) {
        if (vetId < 0) {
            return new DeleteVetApiResponse.DeleteVet400ApiResponse(malformedId(vetId));
        }

        if (vetService.delete(vetId)) {
            return new DeleteVetApiResponse.DeleteVet200ApiResponse(new MessageTO("Successfully deleted Vet with ID: " + vetId));
        } else {
            return new DeleteVetApiResponse.DeleteVet404ApiResponse(notFound(vetId));
        }
    }

    private static MessageTO notFound(long vetId) {
        return new MessageTO("Vet not found for ID: " + vetId);
    }

    private static MessageTO malformedId(long vetId) {
        return new MessageTO("Vet malformed ID: " + vetId);
    }
}

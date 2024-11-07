package ru.tinkoff.kora.example.submodule.app.model;

import org.mapstruct.Mapper;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.VetTO;
import ru.tinkoff.kora.example.submodule.vet.model.dao.Vet;

@Mapper
public interface VetMapper {

    VetTO asDTO(Vet pet);
}

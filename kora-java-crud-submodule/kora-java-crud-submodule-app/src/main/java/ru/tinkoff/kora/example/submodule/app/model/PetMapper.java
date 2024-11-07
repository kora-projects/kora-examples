package ru.tinkoff.kora.example.submodule.app.model;

import org.mapstruct.Mapper;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.CategoryTO;
import ru.tinkoff.kora.example.submodule.openapi.http.server.model.PetTO;
import ru.tinkoff.kora.example.submodule.pet.model.dao.PetCategory;
import ru.tinkoff.kora.example.submodule.pet.model.dao.PetWithCategory;

@Mapper
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}

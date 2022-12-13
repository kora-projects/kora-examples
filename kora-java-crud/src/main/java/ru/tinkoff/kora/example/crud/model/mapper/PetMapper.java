package ru.tinkoff.kora.example.crud.model.mapper;

import org.mapstruct.Mapper;
import ru.tinkoff.kora.example.crud.model.dao.PetCategory;
import ru.tinkoff.kora.example.crud.model.dao.PetWithCategory;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.CategoryTO;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetTO;

@Mapper
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}

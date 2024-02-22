package ru.tinkoff.kora.example.graalvm.crud.model.mapper;

import org.mapstruct.Mapper;
import ru.tinkoff.kora.example.graalvm.crud.model.dao.PetCategory;
import ru.tinkoff.kora.example.graalvm.crud.model.dao.PetWithCategory;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.CategoryTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetTO;

@Mapper
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}

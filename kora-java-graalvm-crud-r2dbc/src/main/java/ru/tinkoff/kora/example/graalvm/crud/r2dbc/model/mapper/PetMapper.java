package ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.mapper;

import org.mapstruct.Mapper;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.CategoryTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetTO;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetCategory;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetWithCategory;

@Mapper
public interface PetMapper {

    PetTO asDTO(PetWithCategory pet);

    CategoryTO asDTO(PetCategory category);
}

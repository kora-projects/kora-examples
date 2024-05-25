package ru.tinkoff.kora.example.graalvm.crud.cassandra.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.CategoryTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetTO;

@Mapper
public interface PetMapper {

    @Mapping(source = "pet", target = "category")
    PetTO asDTO(Pet pet);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "category", target = "name")
    CategoryTO asCategoryTO(Pet pet);
}
